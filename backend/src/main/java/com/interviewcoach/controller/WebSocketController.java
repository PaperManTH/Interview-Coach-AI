package com.interviewcoach.controller;

import com.interviewcoach.entity.dto.common.ApiResponse;
import com.interviewcoach.entity.dto.websocket.MessageDTO;
import com.interviewcoach.entity.dto.websocket.MessageType;
import com.interviewcoach.entity.dto.websocket.SessionStatusDTO;
import com.interviewcoach.websocket.InterviewWsSession;
import com.interviewcoach.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * WebSocket 管理 REST 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ws")
@RequiredArgsConstructor
public class WebSocketController {

    private final WebSocketSessionManager sessionManager;

    /**
     * 向指定 sessionId 发送消息
     *
     * @param sessionId 目标会话 ID
     * @param content   消息内容
     * @return 发送结果
     */
    @PostMapping("/send/{sessionId}")
    public ApiResponse<Boolean> sendToSession(
            @PathVariable String sessionId,
            @RequestBody String content) {
        
        InterviewWsSession ws = sessionManager.findBySessionId(sessionId);
        if (ws == null) {
            return ApiResponse.error(404, "会话不存在: " + sessionId);
        }
        
        MessageDTO message = MessageDTO.builder()
                .id(UUID.randomUUID().toString())
                .type(MessageType.TEXT)
                .sender("system")
                .content(content)
                .timestamp(System.currentTimeMillis())
                .sessionId(sessionId)
                .build();
        
        boolean sent = sessionManager.sendToSession(sessionId, message);
        log.info("[REST] 发送消息到 sessionId={}, 结果={}", sessionId, sent);
        
        return sent 
                ? ApiResponse.ok(true) 
                : ApiResponse.error(500, "发送失败");
    }

    /**
     * 向指定用户的所有会话发送消息
     *
     * @param userId  目标用户 ID
     * @param content 消息内容
     * @return 发送的会话数量
     */
    @PostMapping("/send/user/{userId}")
    public ApiResponse<Integer> sendToUser(
            @PathVariable String userId,
            @RequestBody String content) {
        
        MessageDTO message = MessageDTO.builder()
                .id(UUID.randomUUID().toString())
                .type(MessageType.TEXT)
                .sender("system")
                .receiver(userId)
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
        
        int count = sessionManager.sendToUser(userId, message);
        log.info("[REST] 发送消息到 userId={}, 会话数={}", userId, count);
        
        return ApiResponse.ok(count);
    }

    /**
     * 广播消息到所有在线会话
     *
     * @param content 消息内容
     * @return 发送的会话数量
     */
    @PostMapping("/broadcast")
    public ApiResponse<Integer> broadcast(@RequestBody String content) {
        MessageDTO message = MessageDTO.builder()
                .id(UUID.randomUUID().toString())
                .type(MessageType.TEXT)
                .sender("system")
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
        
        int count = sessionManager.broadcast(message);
        log.info("[REST] 广播消息到 {} 个会话", count);
        
        return ApiResponse.ok(count);
    }

    /**
     * 查询指定会话状态
     *
     * @param sessionId 会话 ID
     * @return 会话状态信息
     */
    @GetMapping("/session/{sessionId}")
    public ApiResponse<SessionStatusDTO> getSession(@PathVariable String sessionId) {
        InterviewWsSession ws = sessionManager.findBySessionId(sessionId);
        if (ws == null) {
            return ApiResponse.error(404, "会话不存在: " + sessionId);
        }
        return ApiResponse.ok(toStatusDTO(ws));
    }

    /**
     * 查询指定用户的所有会话
     *
     * @param userId 用户 ID
     * @return 会话列表
     */
    @GetMapping("/user/{userId}/sessions")
    public ApiResponse<List<SessionStatusDTO>> getUserSessions(@PathVariable String userId) {
        List<SessionStatusDTO> sessions = sessionManager.findByUserId(userId).stream()
                .map(this::toStatusDTO)
                .toList();
        return ApiResponse.ok(sessions);
    }

    /**
     * 查询所有在线会话
     *
     * @return 所有在线会话列表
     */
    @GetMapping("/sessions")
    public ApiResponse<List<SessionStatusDTO>> getAllSessions() {
        List<SessionStatusDTO> sessions = sessionManager.allSessions().stream()
                .map(this::toStatusDTO)
                .toList();
        return ApiResponse.ok(sessions);
    }

    /**
     * 查询在线会话数量
     *
     * @return 在线会话数量
     */
    @GetMapping("/online-count")
    public ApiResponse<Integer> getOnlineCount() {
        return ApiResponse.ok(sessionManager.onlineCount());
    }

    /**
     * 转换 InterviewWsSession 到 SessionStatusDTO
     */
    private SessionStatusDTO toStatusDTO(InterviewWsSession ws) {
        return SessionStatusDTO.builder()
                .sessionId(ws.getSessionId())
                .userId(ws.getUserId())
                .online(ws.isOpen())
                .connectedAt(ws.getConnectedAt())
                .lastHeartbeatAt(ws.getLastHeartbeatAt())
                .messageCount(ws.getMessageCount().get())
                .build();
    }
}