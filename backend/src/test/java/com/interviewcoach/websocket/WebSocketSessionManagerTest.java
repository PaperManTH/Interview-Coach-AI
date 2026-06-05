package com.interviewcoach.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebSocket SessionManager 测试类。
 */
class WebSocketSessionManagerTest {

    private WebSocketSessionManager manager;

    @BeforeEach
    void setUp() {
        manager = new WebSocketSessionManager();
    }

    @Test
    void testOnlineCount() {
        assertEquals(0, manager.onlineCount());
    }

    @Test
    void testPongMessage() {
        String sessionId = "test-session-001";
        var pong = manager.pong(sessionId);
        
        assertNotNull(pong);
        assertEquals("pong", pong.getContent());
        assertEquals("test-session-001", pong.getSessionId());
    }

    @Test
    void testFindByUserIdEmpty() {
        var sessions = manager.findByUserId("unknown-user");
        assertNotNull(sessions);
        assertTrue(sessions.isEmpty());
    }

    @Test
    void testAllSessionsEmpty() {
        var sessions = manager.allSessions();
        assertNotNull(sessions);
        assertTrue(sessions.isEmpty());
    }
}
