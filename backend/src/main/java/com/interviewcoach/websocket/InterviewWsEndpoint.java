package com.interviewcoach.websocket;

import com.interviewcoach.entity.InterviewSession;
import com.interviewcoach.entity.dto.ai.BilingualResponse;
import com.interviewcoach.entity.dto.websocket.MessageDTO;
import com.interviewcoach.entity.dto.websocket.MessageType;
import com.interviewcoach.service.AsrService;
import com.interviewcoach.service.ConversationService;
import com.interviewcoach.service.SpringAiService;
import com.interviewcoach.service.TtsService;
import com.interviewcoach.utils.MessageCodec;

import com.interviewcoach.entity.InterviewMessage;
import lombok.extern.slf4j.Slf4j;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
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
    private ConversationService conversationService;

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

    public void setConversationService(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        String websocketSessionId = session.getId();
        
        if (sessionManager == null) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Server error"));
            } catch (IOException ignored) {}
            return;
        }
        
        String userId = extractUserId(session, config);
        session.getUserProperties().put(WebSocketSessionManager.KEY_USER_ID, userId);
        sessionManager.register(session);
        
        // 创建数据库会话记录（用于消息持久化）
        String dbSessionId = null;
        log.info("[WS] conversationService = {}", conversationService);
        if (conversationService != null) {
            try {
                // 先检查用户是否已有活跃会话，如果有则复用
                InterviewSession existingSession = conversationService.getActiveSession(userId);
                if (existingSession != null) {
                    dbSessionId = existingSession.getSessionId();
                    log.info("[WS] 复用用户活跃会话: dbSessionId={}, userId={}", dbSessionId, userId);
                } else {
                    InterviewSession dbSession = conversationService.createSession(userId, "dynamic");
                    dbSessionId = dbSession.getSessionId();
                    log.info("[WS] 数据库会话创建成功: dbSessionId={}, userId={}", dbSessionId, userId);
                }
                session.getUserProperties().put("dbSessionId", dbSessionId);
            } catch (Exception e) {
                log.error("[WS] 数据库会话创建/复用失败", e);
            }
        } else {
            log.warn("[WS] conversationService 为 null，无法创建数据库会话");
        }
        
        log.info("[WS] 连接建立 websocketSessionId={}, userId={}, dbSessionId={}", websocketSessionId, userId, dbSessionId);
        sendWelcomeMessage(session, userId);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        String dbSessionId = (String) session.getUserProperties().get("dbSessionId");
        
        // 结束数据库会话
        if (conversationService != null && dbSessionId != null) {
            try {
                conversationService.endSession(dbSessionId);
                log.info("[WS] 数据库会话已结束: dbSessionId={}", dbSessionId);
            } catch (Exception e) {
                log.warn("[WS] 结束数据库会话失败: {}", e.getMessage());
            }
        }
        
        if (sessionManager != null) {
            sessionManager.unregister(session);
            log.info("[WS] 连接关闭 websocketSessionId={}, reason={}", session.getId(), reason);
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
        String websocketSessionId = session.getId();
        String dbSessionId = (String) session.getUserProperties().get("dbSessionId");
        log.info("[WS] 收到消息 websocketSessionId={}, dbSessionId={}, userId={}, content={}", websocketSessionId, dbSessionId, userId, userMessage);

        // 如果 dbSessionId 为空，动态创建数据库会话
        if ((dbSessionId == null || dbSessionId.isEmpty()) && conversationService != null) {
            try {
                InterviewSession existingSession = conversationService.getActiveSession(userId);
                if (existingSession != null) {
                    dbSessionId = existingSession.getSessionId();
                } else {
                    InterviewSession dbSession = conversationService.createSession(userId, "dynamic");
                    dbSessionId = dbSession.getSessionId();
                }
                session.getUserProperties().put("dbSessionId", dbSessionId);
                log.info("[WS] 动态创建/复用会话: dbSessionId={}", dbSessionId);
            } catch (Exception e) {
                log.warn("[WS] 动态创建会话失败: {}", e.getMessage());
            }
        }

        // 1. 先将用户消息回传给前端（让用户看到自己发送的消息）
        MessageDTO userMsg = MessageDTO.builder()
                .id(UUID.randomUUID().toString())
                .type(MessageType.TEXT)
                .sender("user")  // 明确标记为用户消息
                .content(userMessage)
                .timestamp(System.currentTimeMillis())
                .sessionId(websocketSessionId)
                .build();
        sendMessage(session, userMsg);

        // 1.5 持久化用户消息（使用数据库会话ID）
        log.info("[WS-Persistence] 保存用户消息: messageId={}, dbSessionId={}", userMsg.getId(), dbSessionId);
        saveMessageToDatabase(dbSessionId, userId, userMsg.getId(), "user", "text", userMessage);

        // 2. 生成 AI 双语回复（传递原始 msg 以提取动态模式 metadata）
        BilingualResponse aiResponse = callAiServiceBilingual(userId, userMessage, msg);

        // 3. 构建双语消息发送给前端
        Map<String, Object> bilingualContent = new HashMap<>();
        bilingualContent.put("chinese", aiResponse.getChinese());
        bilingualContent.put("english", aiResponse.getEnglish());

        MessageDTO reply = MessageDTO.builder()
                .id(UUID.randomUUID().toString())
                .type(MessageType.TEXT)
                .sender("assistant")
                .receiver(msg.getSender())
                .content(aiResponse.getFormattedText())  // 兼容旧客户端，显示双语文本
                .timestamp(System.currentTimeMillis())
                .sessionId(websocketSessionId)
                .metadata(bilingualContent)  // 新客户端可以使用结构化数据
                .build();
        sendMessage(session, reply);
        sendMessage(session, MessageDTO.ack(msg.getId()));

        // 3.5 持久化 AI 回复消息（使用数据库会话ID）
        String metadataJson = null;
        try {
            metadataJson = MessageCodec.toJson(bilingualContent);
        } catch (Exception e) {
            log.warn("[WS-Persistence] metadata 序列化失败: {}", e.getMessage());
        }
        log.info("[WS-Persistence] 保存 AI 回复: messageId={}, dbSessionId={}", reply.getId(), dbSessionId);
        saveMessageToDatabase(dbSessionId, userId, reply.getId(), "assistant", "text", aiResponse.getFormattedText(), metadataJson);

        // 4. TTS 语音合成（仅处理英文部分，过滤疑似降级/错误提示）
        String englishText = aiResponse.getEnglish();
        if (shouldSkipTts(englishText)) {
            log.debug("[WS-TTS] 跳过语音合成（响应疑似降级/错误提示）");
        } else {
            synthesizeAndSend(session, userId, englishText);
        }
    }

    /**
     * 将消息持久化到数据库
     */
    private void saveMessageToDatabase(String sessionId, String userId, String messageId, 
                                        String sender, String type, String content) {
        saveMessageToDatabase(sessionId, userId, messageId, sender, type, content, null);
    }

    /**
     * 将消息持久化到数据库（带 metadata）
     */
    private void saveMessageToDatabase(String sessionId, String userId, String messageId,
                                        String sender, String type, String content, String metadata) {
        if (conversationService == null) {
            log.warn("[WS-Persistence] ConversationService 未初始化，跳过消息持久化");
            return;
        }
        if (sessionId == null || sessionId.isEmpty()) {
            log.warn("[WS-Persistence] sessionId 为空，跳过消息持久化 (sender={}, content={})", sender, content);
            return;
        }
        try {
            InterviewMessage message = new InterviewMessage();
            message.setSessionId(sessionId);
            message.setMessageId(messageId);
            message.setSender(sender);
            message.setType(type);
            message.setContent(content);
            message.setTimestampMs(System.currentTimeMillis());
            message.setMetadata(metadata);
            conversationService.saveMessage(message);
            log.info("[WS-Persistence] 消息持久化成功: messageId={}, sender={}, sessionId={}", messageId, sender, sessionId);
        } catch (Exception e) {
            log.error("[WS-Persistence] 消息持久化失败: {}", e.getMessage(), e);
        }
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

        // ===== 语音识别失败时直接降级处理，不继续调用 LLM 和 TTS =====
        if (voiceText == null || voiceText.isEmpty() || 
            voiceText.contains("失败") || voiceText.contains("未就绪") || 
            voiceText.contains("error") || voiceText.contains("Error") ||
            voiceText.contains("识别失败") || voiceText.contains("暂无结果")) {
            log.warn("[WS] 语音识别失败，跳过 LLM 和 TTS 流程: {}", voiceText);
            // 发送错误提示消息
            MessageDTO errorMsg = MessageDTO.builder()
                    .id(UUID.randomUUID().toString())
                    .type(MessageType.TEXT)
                    .sender("system")
                    .content("抱歉，语音识别失败，请重试或使用文字输入")
                    .timestamp(System.currentTimeMillis())
                    .sessionId(session.getId())
                    .build();
            sendMessage(session, errorMsg);
            return; // 直接返回，不继续后续流程
        }

        // 2. 生成 AI 双语回复（传递原始 msg 以提取动态模式 metadata）
        BilingualResponse aiResponse = callAiServiceBilingual(userId, voiceText, msg);

        // 3. 构建双语消息发送给前端
        Map<String, Object> bilingualContent = new HashMap<>();
        bilingualContent.put("chinese", aiResponse.getChinese());
        bilingualContent.put("english", aiResponse.getEnglish());

        MessageDTO reply = MessageDTO.builder()
                .id(UUID.randomUUID().toString())
                .type(MessageType.TEXT)
                .sender("assistant")
                .content(aiResponse.getFormattedText())  // 兼容旧客户端
                .timestamp(System.currentTimeMillis())
                .sessionId(session.getId())
                .metadata(bilingualContent)  // 新客户端可以使用结构化数据
                .build();
        sendMessage(session, reply);

        // 4. TTS（仅处理英文部分，过滤疑似降级/错误提示）
        String englishText = aiResponse.getEnglish();
        if (shouldSkipTts(englishText)) {
            log.debug("[WS-TTS] 跳过语音合成（响应疑似降级/错误提示）");
        } else {
            synthesizeAndSend(session, userId, englishText);
        }
    }

    private void handleAck(Session session, MessageDTO msg) {
        log.debug("[WS] 收到 ACK msgId={}", msg.getId());
    }

    private String callAiService(String userId, String message) {
        return callAiService(userId, message, null);
    }

    /**
     * 调用 AI 服务，支持动态模式（带阶段信息）。
     * @param userId 用户 ID
     * @param message 用户消息
     * @param msg 原始 MessageDTO（用于提取 metadata 中的阶段信息），可为 null
     */
    private String callAiService(String userId, String message, MessageDTO msg) {
        if (springAiService == null) {
            log.warn("[WS] SpringAiService 未初始化，使用模拟回复");
            return "模拟回复：" + message;
        }
        try {
            // 检查是否为动态模式
            if (msg != null && msg.getMetadata() != null) {
                Object modeObj = msg.getMetadata().get("mode");
                if ("dynamic".equals(modeObj)) {
                    String stage = String.valueOf(msg.getMetadata().getOrDefault("stage", "warmup"));
                    String focus = String.valueOf(msg.getMetadata().getOrDefault("focus", "general"));
                    log.info("[WS-Dynamic] 检测到动态模式, stage={}, focus={}", stage, focus);
                    return springAiService.chatDynamic(userId, message, stage, focus);
                }
            }
            // 传统模式
            return springAiService.chat(userId, message);
        } catch (Exception e) {
            log.error("[WS] AI 服务调用失败", e);
            return "抱歉，服务暂时不可用";
        }
    }

    /**
     * 调用 AI 服务并返回双语响应。
     * @param userId 用户 ID
     * @param message 用户消息
     * @param msg 原始 MessageDTO（用于提取 metadata 中的阶段信息），可为 null
     * @return BilingualResponse 双语响应对象
     */
    private BilingualResponse callAiServiceBilingual(String userId, String message, MessageDTO msg) {
        if (springAiService == null) {
            log.warn("[WS] SpringAiService 未初始化，使用模拟回复");
            return new BilingualResponse("模拟回复：" + message, "Mock response: " + message);
        }
        try {
            // 检查是否为动态模式
            if (msg != null && msg.getMetadata() != null) {
                Object modeObj = msg.getMetadata().get("mode");
                if ("dynamic".equals(modeObj)) {
                    String stage = String.valueOf(msg.getMetadata().getOrDefault("stage", "warmup"));
                    String focus = String.valueOf(msg.getMetadata().getOrDefault("focus", "general"));
                    log.info("[WS-Dynamic] 检测到动态模式, stage={}, focus={}", stage, focus);
                    return springAiService.chatDynamicBilingual(userId, message, stage, focus);
                }
            }
            // 传统模式
            return springAiService.chatBilingual(userId, message);
        } catch (Exception e) {
            log.error("[WS] AI 服务调用失败", e);
            return new BilingualResponse("抱歉，服务暂时不可用", "Sorry, service is temporarily unavailable");
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

    /**
     * 发送阶段切换通知（动态面试模式）。
     * 前端收到后会自动推进到下一阶段。
     */
    private void sendStageTransition(Session session, String nextStage, String transitionMessage) {
        try {
            String payload = String.format(
                    "{\"stage\":\"%s\",\"transitionMessage\":\"%s\"}",
                    nextStage, transitionMessage != null ? transitionMessage : ""
            );
            sendMessage(session, MessageDTO.builder()
                    .id(UUID.randomUUID().toString())
                    .type(MessageType.STAGE_TRANSITION)
                    .sender("system")
                    .content(payload)
                    .timestamp(System.currentTimeMillis())
                    .sessionId(session.getId())
                    .build());
            log.info("[WS] 已发送阶段切换通知: stage={}", nextStage);
        } catch (Exception e) {
            log.error("[WS] 发送阶段切换通知失败", e);
        }
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

    /**
     * 判断是否应跳过 TTS 合成：空/空白、短文本（<10）、疑似错误提示。
     */
    private static boolean shouldSkipTts(String text) {
        if (isBlank(text)) return true;
        if (text.length() < 10) return true;
        String lower = text.toLowerCase();
        return lower.contains("sorry")
                || lower.contains("service")
                || lower.contains("temporarily")
                || lower.contains("unavailable");
    }
}
