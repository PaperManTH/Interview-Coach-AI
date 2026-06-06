package com.interviewcoach.websocket;

import com.interviewcoach.entity.dto.websocket.MessageDTO;
import com.interviewcoach.entity.dto.websocket.MessageType;
import com.interviewcoach.service.AsrService;
import com.interviewcoach.service.SpringAiService;
import com.interviewcoach.service.TtsService;
import com.interviewcoach.util.MessageCodec;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;

/**
 * WebSocket 服务端点（JSR 356）。
 * 路径：ws://host:port/ws/interview
 * <p>
 * 职责：处理连接生命周期、消息解析、AI 对话。
 */
@Slf4j
@ServerEndpoint(value = "/ws/interview", configurator = WebSocketConfigurator.class)
public class InterviewWsEndpoint {

    private WebSocketSessionManager sessionManager;
    private SpringAiService springAiService;
    private AsrService asrService;
    private TtsService ttsService;

    public void setSessionManager(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setSpringAiService(SpringAiService springAiService) {
        this.springAiService = springAiService;
    }

    public void setAsrService(AsrService asrService) {
        this.asrService = asrService;
    }

    public void setTtsService(TtsService ttsService) {
        this.ttsService = ttsService;
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        String sessionId = session.getId();
        
        if (sessionManager == null) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Server error"));
            } catch (IOException ignored) {}
            return;
        }
        
        String userId = extractUserId(session, config);
        session.getUserProperties().put(WebSocketSessionManager.KEY_USER_ID, userId);
        sessionManager.register(session);
        
        log.info("[WS] 连接建立 sessionId={}, userId={}", sessionId, userId);
        sendWelcomeMessage(session, userId);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        if (sessionManager != null) {
            sessionManager.unregister(session);
            log.info("[WS] 连接关闭 sessionId={}, reason={}", session.getId(), reason);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("[WS] 连接异常 sessionId={}", session.getId(), error);
        WebSocketSessionManager.safeClose(session);
    }

    @OnMessage(maxMessageSize = 10485760)
    public void onMessage(Session session, String message) {
        if (sessionManager == null) return;
        
        sessionManager.touchHeartbeat(session.getId());
        
        try {
            MessageDTO msg = MessageCodec.deserialize(message);
            handleMessage(session, msg);
        } catch (Exception e) {
            log.warn("[WS] 消息解析失败: {}", e.getMessage());
            sendError(session, "消息格式错误");
        }
    }

    @OnMessage(maxMessageSize = 10485760)
    public void onBinary(Session session, byte[] data, boolean last) {
        log.debug("[WS] 收到二进制消息 len={}, last={}", data.length, last);
    }

    private void handleMessage(Session session, MessageDTO msg) throws IOException {
        if (msg.getType() == null) {
            sendError(session, "消息类型不能为空");
            return;
        }
        
        switch (msg.getType()) {
            case HEARTBEAT -> handleHeartbeat(session, msg);
            case TEXT, CHAT -> handleTextMessage(session, msg);
            case ACK -> handleAck(session, msg);
            case VOICE_START -> handleVoiceStart(session, msg);
            case VOICE_CHUNK -> handleVoiceChunk(session, msg);
            case VOICE_END -> handleVoiceEnd(session, msg);
            default -> sendError(session, "不支持的消息类型: " + msg.getType());
        }
    }

    private void handleHeartbeat(Session session, MessageDTO msg) {
        sendMessage(session, sessionManager.pong(session.getId()));
    }

    private void handleTextMessage(Session session, MessageDTO msg) {
        String userMessage = msg.getContent();
        String userId = getUserId(session);
        log.info("[WS] 收到消息 sessionId={}, content={}", session.getId(), userMessage);

        // 1. 先将用户消息回传给前端（让用户看到自己发送的消息）
        MessageDTO userMsg = MessageDTO.builder()
                .id(UUID.randomUUID().toString())
                .type(MessageType.TEXT)
                .sender("user")  // 明确标记为用户消息
                .content(userMessage)
                .timestamp(System.currentTimeMillis())
                .sessionId(session.getId())
                .build();
        sendMessage(session, userMsg);

        // 2. 生成 AI 回复
        String aiResponse = callAiService(userId, userMessage);

        MessageDTO reply = MessageDTO.builder()
                .id(UUID.randomUUID().toString())
                .type(MessageType.TEXT)
                .sender("assistant")
                .receiver(msg.getSender())
                .content(aiResponse)
                .timestamp(System.currentTimeMillis())
                .sessionId(session.getId())
                .build();
        sendMessage(session, reply);
        sendMessage(session, MessageDTO.ack(msg.getId()));

        // 3. TTS 语音合成（异步，不阻塞回复）
        synthesizeAndSend(session, userId, aiResponse);
    }

    private void handleVoiceStart(Session session, MessageDTO msg) {
        log.info("[WS] 开始语音输入 sessionId={}", session.getId());
        sendMessage(session, MessageDTO.builder()
                .id(UUID.randomUUID().toString())
                .type(MessageType.TEXT)
                .sender("system")
                .content("开始录音...")
                .timestamp(System.currentTimeMillis())
                .sessionId(session.getId())
                .build());
    }

    private void handleVoiceChunk(Session session, MessageDTO msg) throws IOException {
        // 累积 Base64 编码的音频片段
        String chunk = msg.getContent();
        if (chunk != null && !chunk.isEmpty()) {
            @SuppressWarnings("unchecked")
            var buf = (java.io.ByteArrayOutputStream) session.getUserProperties().get("audioBuffer");
            if (buf == null) {
                buf = new java.io.ByteArrayOutputStream();
                session.getUserProperties().put("audioBuffer", buf);
            }
            try {
                byte[] decoded = Base64.getDecoder().decode(chunk);
                buf.write(decoded);
                log.debug("[WS] 累积音频 chunk {} bytes, total {} bytes", decoded.length, buf.size());
            } catch (IllegalArgumentException e) {
                log.warn("[WS] 音频 chunk Base64 解码失败: {}", e.getMessage());
            }
        }
    }

    private void handleVoiceEnd(Session session, MessageDTO msg) {
        String userId = getUserId(session);
        log.info("[WS] 结束语音输入 sessionId={}, userId={}", session.getId(), userId);

        // 获取累积的音频数据
        @SuppressWarnings("unchecked")
        var buf = (java.io.ByteArrayOutputStream) session.getUserProperties().remove("audioBuffer");
        Path audioPath = null;
        int audioSize = 0;

        if (buf != null && buf.size() > 0) {
            try {
                audioPath = Files.createTempFile("iflytek_asr_", ".pcm");
                Files.write(audioPath, buf.toByteArray());
                audioSize = buf.size();
                log.info("[WS] 音频临时文件: {} ({} bytes)", audioPath, audioSize);
            } catch (IOException e) {
                log.error("[WS] 写入音频临时文件失败", e);
            }
        }

        // 如果前端直接发了完整音频（VOICE_END 携带 content）
        if (audioPath == null && msg.getContent() != null && !msg.getContent().isEmpty()) {
            try {
                byte[] audioBytes = Base64.getDecoder().decode(msg.getContent());
                audioPath = Files.createTempFile("iflytek_asr_", ".pcm");
                Files.write(audioPath, audioBytes);
                audioSize = audioBytes.length;
                log.info("[WS] 音频临时文件(来自 content): {} ({} bytes)", audioPath, audioBytes.length);
            } catch (Exception e) {
                log.error("[WS] 写入音频临时文件失败", e);
            }
        }

        String voiceText;
        if (audioPath != null && asrService != null) {
            voiceText = asrService.transcribe(userId, audioPath);
            try { Files.deleteIfExists(audioPath); } catch (IOException ignored) {}
        } else if (asrService != null) {
            voiceText = asrService.transcribe(userId, null);
        } else {
            voiceText = "语音识别服务未就绪";
        }

        log.info("[WS] ASR 结果: {}", voiceText);

        // 1. 先将语音识别文字作为用户消息回传到前端（用 VOICE_TEXT 类型标识）
        MessageDTO voiceTextMsg = MessageDTO.builder()
                .id(UUID.randomUUID().toString())
                .type(MessageType.VOICE_TEXT)
                .sender(msg.getSender() != null ? msg.getSender() : "user")
                .content(voiceText != null ? voiceText : "语音识别失败")
                .timestamp(System.currentTimeMillis())
                .sessionId(session.getId())
                .build();
        sendMessage(session, voiceTextMsg);

        // 2. 生成 AI 回复
        String aiResponse = callAiService(userId, voiceText);

        MessageDTO reply = MessageDTO.builder()
                .id(UUID.randomUUID().toString())
                .type(MessageType.TEXT)
                .sender("assistant")
                .content(aiResponse)
                .timestamp(System.currentTimeMillis())
                .sessionId(session.getId())
                .build();
        sendMessage(session, reply);

        // 3. TTS
        synthesizeAndSend(session, userId, aiResponse);
    }

    private void handleAck(Session session, MessageDTO msg) {
        log.debug("[WS] 收到 ACK msgId={}", msg.getId());
    }

    private String callAiService(String userId, String message) {
        if (springAiService == null) {
            log.warn("[WS] SpringAiService 未初始化，使用模拟回复");
            return "模拟回复：" + message;
        }
        try {
            return springAiService.chat(userId, message);
        } catch (Exception e) {
            log.error("[WS] AI 服务调用失败", e);
            return "抱歉，服务暂时不可用";
        }
    }

    private void sendMessage(Session session, MessageDTO msg) {
        try {
            session.getBasicRemote().sendText(MessageCodec.serialize(msg));
        } catch (IOException e) {
            log.error("[WS] 发送消息失败", e);
        }
    }

    private void sendError(Session session, String errorMessage) {
        sendMessage(session, MessageDTO.builder()
                .id(UUID.randomUUID().toString())
                .type(MessageType.ERROR)
                .sender("system")
                .content(errorMessage)
                .timestamp(System.currentTimeMillis())
                .sessionId(session.getId())
                .build());
    }

    private void sendWelcomeMessage(Session session, String userId) {
        sendMessage(session, MessageDTO.builder()
                .id(UUID.randomUUID().toString())
                .type(MessageType.TEXT)
                .sender("system")
                .receiver(userId)
                .content("连接成功！欢迎使用 Interview Coach")
                .timestamp(System.currentTimeMillis())
                .sessionId(session.getId())
                .build());
    }

    private String getUserId(Session session) {
        Object uid = session.getUserProperties().get(WebSocketSessionManager.KEY_USER_ID);
        return uid != null ? uid.toString() : "anonymous";
    }

    private String extractUserId(Session session, EndpointConfig config) {
        var params = session.getRequestParameterMap().get("userId");
        if (params != null && !params.isEmpty()) {
            return params.get(0);
        }
        if (config != null) {
            Object userIdObj = config.getUserProperties().get(WebSocketSessionManager.KEY_USER_ID);
            if (userIdObj != null) return userIdObj.toString();
        }
        return "anonymous";
    }

    /**
     * TTS 合成并发送音频消息。
     */
    private void synthesizeAndSend(Session session, String userId, String text) {
        if (ttsService == null || isBlank(text)) return;
        try {
            String audioBase64 = ttsService.synthesize(userId, text);
            if (audioBase64 != null) {
                MessageDTO audioMsg = MessageDTO.builder()
                        .id(UUID.randomUUID().toString())
                        .type(MessageType.AUDIO)
                        .sender("assistant")
                        .content(audioBase64)
                        .timestamp(System.currentTimeMillis())
                        .sessionId(session.getId())
                        .build();
                sendMessage(session, audioMsg);
            }
        } catch (Exception e) {
            log.warn("[WS] TTS 合成失败: {}", e.getMessage());
        }
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}
