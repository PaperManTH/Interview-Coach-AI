package com.interviewcoach.websocket;

import jakarta.websocket.Session;
import lombok.Getter;
import lombok.ToString;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 对 {@link Session} 的业务包装：记录 userId、连接时间、最后心跳时间、消息计数。
 */
@Getter
@ToString(exclude = "session")
public class InterviewWsSession {

    private final String sessionId;
    private final String userId;
    private final Session session;
    private final long connectedAt;
    private volatile long lastHeartbeatAt;
    private final AtomicLong messageCount = new AtomicLong(0);

    public InterviewWsSession(String sessionId, String userId, Session session) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.session = session;
        this.connectedAt = System.currentTimeMillis();
        this.lastHeartbeatAt = connectedAt;
    }

    public boolean isOpen() {
        return session != null && session.isOpen();
    }

    public void touch() {
        lastHeartbeatAt = System.currentTimeMillis();
    }

    public long onMessageSent() {
        return messageCount.incrementAndGet();
    }
}
