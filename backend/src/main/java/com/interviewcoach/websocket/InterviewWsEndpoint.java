package com.interviewcoach.websocket;

import com.interviewcoach.dto.websocket.MessageDTO;
import com.interviewcoach.dto.websocket.MessageType;
import com.interviewcoach.service.SpringAiService;
import com.interviewcoach.util.MessageCodec;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
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

    public void setSessionManager(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setSpringAiService(SpringAiService springAiService) {
        this.springAiService = springAiService;
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

    @OnMessage
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

    @OnMessage
    public void onBinary(Session session, byte[] data, boolean last) {
        log.debug("[WS] 收到二进制消息 len={}, last={}", data.length, last);
    }

    private void handleMessage(Session session, MessageDTO msg) {
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
        log.info("[WS] 收到消息 sessionId={}, content={}", session.getId(), userMessage);
        
        String aiResponse = callAiService(userMessage);
        
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

    private void handleVoiceChunk(Session session, MessageDTO msg) {
        log.debug("[WS] 收到语音片段 sessionId={}", session.getId());
    }

    private void handleVoiceEnd(Session session, MessageDTO msg) {
        log.info("[WS] 结束语音输入 sessionId={}", session.getId());
        
        String mockResult = "这是语音识别结果的模拟回复";
        String aiResponse = callAiService(mockResult);
        
        MessageDTO reply = MessageDTO.builder()
                .id(UUID.randomUUID().toString())
                .type(MessageType.TEXT)
                .sender("assistant")
                .content(aiResponse)
                .timestamp(System.currentTimeMillis())
                .sessionId(session.getId())
                .build();
        sendMessage(session, reply);
    }

    private void handleAck(Session session, MessageDTO msg) {
        log.debug("[WS] 收到 ACK msgId={}", msg.getId());
    }

    private String callAiService(String message) {
        if (springAiService == null) {
            log.warn("[WS] SpringAiService 未初始化，使用模拟回复");
            return "模拟回复：" + message;
        }
        
        try {
            return springAiService.chat(message);
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
                .content("连接成功！欢迎使用面试教练 AI，请问有什么可以帮助你的？")
                .timestamp(System.currentTimeMillis())
                .sessionId(session.getId())
                .build());
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
}
