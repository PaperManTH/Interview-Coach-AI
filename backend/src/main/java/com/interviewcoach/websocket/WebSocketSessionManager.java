package com.interviewcoach.websocket;

import com.interviewcoach.common.Constants;
import com.interviewcoach.entity.dto.websocket.MessageDTO;
import com.interviewcoach.entity.dto.websocket.MessageType;
import com.interviewcoach.utils.MessageCodec;

import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * WebSocket 会话管理器：负责 Session 的集中管理、心跳检测、超时清理。
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    /** 单例实例，供 WebSocketConfigurator 获取 */
    private static WebSocketSessionManager instance;

    public WebSocketSessionManager() {
        instance = this;
    }

    private final Map<String, InterviewWsSession> sessionsById = new ConcurrentHashMap<>();
    private final Map<String, CopyOnWriteArrayList<String>> sessionIdsByUser = new ConcurrentHashMap<>();

    public static final String KEY_USER_ID = "userId";

    public static WebSocketSessionManager getInstance() {
        return instance;
    }

    // ========== 注册/注销 ==========

    public void register(Session session) {
        String sessionId = session.getId();
        String userId = getUserId(session);

        InterviewWsSession wrapped = new InterviewWsSession(sessionId, userId, session);
        InterviewWsSession previous = sessionsById.put(sessionId, wrapped);
        if (previous != null) {
            safeClose(previous.getSession());
        }
        sessionIdsByUser.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(sessionId);
        log.info("[WS] 注册 sessionId={}, userId={}, 在线={}", sessionId, userId, sessionsById.size());
    }

    public void unregister(Session session) {
        InterviewWsSession removed = sessionsById.remove(session.getId());
        if (removed == null) return;

        String userId = removed.getUserId();
        CopyOnWriteArrayList<String> list = sessionIdsByUser.get(userId);
        if (list != null) {
            list.remove(session.getId());
            if (list.isEmpty()) sessionIdsByUser.remove(userId);
        }
        log.info("[WS] 注销 sessionId={}, 在线={}", session.getId(), sessionsById.size());
    }

    // ========== 查询 ==========

    public int onlineCount() {
        return sessionsById.size();
    }

    public Collection<InterviewWsSession> allSessions() {
        return sessionsById.values();
    }

    public InterviewWsSession findBySessionId(String sessionId) {
        return sessionsById.get(sessionId);
    }

    public Collection<InterviewWsSession> findByUserId(String userId) {
        CopyOnWriteArrayList<String> ids = sessionIdsByUser.get(userId);
        if (ids == null || ids.isEmpty()) return java.util.Collections.emptyList();
        return ids.stream().map(sessionsById::get).filter(java.util.Objects::nonNull).toList();
    }

    // ========== 发送 ==========

    public boolean sendToSession(String sessionId, MessageDTO message) {
        InterviewWsSession ws = sessionsById.get(sessionId);
        if (ws == null || !ws.isOpen()) return false;
        return doSend(ws, message);
    }

    public int sendToUser(String userId, MessageDTO message) {
        return (int) findByUserId(userId).stream().filter(InterviewWsSession::isOpen).filter(ws -> doSend(ws, message)).count();
    }

    public int broadcast(MessageDTO message) {
        return (int) sessionsById.values().stream().filter(InterviewWsSession::isOpen).filter(ws -> doSend(ws, message)).count();
    }

    private boolean doSend(InterviewWsSession ws, MessageDTO message) {
        try {
            ws.getSession().getBasicRemote().sendText(MessageCodec.serialize(message));
            ws.onMessageSent();
            return true;
        } catch (IOException e) {
            log.error("[WS] 发送失败 sessionId={}", ws.getSessionId(), e);
            return false;
        }
    }

    // ========== 心跳与超时清理（每 30s 扫描一次） ==========

    @Scheduled(fixedRate = 30_000L)
    public void cleanIdleSessions() {
        long now = System.currentTimeMillis();
        long idleThreshold = 90_000L;
        sessionsById.values().removeIf(ws -> {
            if (now - ws.getLastHeartbeatAt() > idleThreshold) {
                log.warn("[WS] 会话超时 sessionId={}, userId={}", ws.getSessionId(), ws.getUserId());
                safeClose(ws.getSession());
                return true;
            }
            if (!ws.isOpen()) return true;
            return false;
        });
    }

    public void touchHeartbeat(String sessionId) {
        InterviewWsSession ws = sessionsById.get(sessionId);
        if (ws != null) ws.touch();
    }

    public MessageDTO pong(String sessionId) {
        return MessageDTO.builder()
                .type(MessageType.PONG)
                .sessionId(sessionId)
                .content("pong")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    static void safeClose(Session session) {
        if (session == null) return;
        try {
            if (session.isOpen()) session.close();
        } catch (IOException ignored) {}
    }

    private String getUserId(Session session) {
        Object userIdAttr = session.getUserProperties().get(KEY_USER_ID);
        return userIdAttr == null ? Constants.DEFAULT_USER_ID : userIdAttr.toString();
    }
}
