package com.interviewcoach.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.interviewcoach.entity.InterviewMessage;
import com.interviewcoach.entity.InterviewSession;
import com.interviewcoach.service.ConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对话记忆 REST API 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/conversation")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    /**
     * 创建会话。
     * @param userId        用户 ID
     * @param interviewType 面试类型
     * @return  创建的会话信息
     */
    @PostMapping("/sessions")
    public ResponseEntity<Map<String, Object>> createSession(
            @RequestParam String userId,
            @RequestParam(required = false, defaultValue = "general") String interviewType) {
        
        InterviewSession session = conversationService.createSession(userId, interviewType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("session", session);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取会话详情。
     * @param sessionId  会话 ID
     * @return  会话信息
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSession(@PathVariable String sessionId) {
        InterviewSession session = conversationService.getSession(sessionId);
        
        Map<String, Object> response = new HashMap<>();
        if (session != null) {
            response.put("success", true);
            response.put("session", session);
        } else {
            response.put("success", false);
            response.put("message", "会话不存在");
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户的会话列表（分页）。
     * @param userId  用户 ID
     * @param page    页码
     * @param size    每页大小
     * @return  会话列表及分页信息
     */
    @GetMapping("/sessions/user/{userId}")
    public ResponseEntity<Map<String, Object>> listSessionsByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        IPage<InterviewSession> sessions = conversationService.listSessionsByUser(userId, page, size);
        
        // 调试日志
        if (sessions.getRecords() != null) {
            for (InterviewSession s : sessions.getRecords()) {
                log.info("[Controller] 会话列表: sessionId={}, status={}, currentStage={}, stageRound={}, totalRounds={}",
                        s.getSessionId(), s.getStatus(), s.getCurrentStage(), s.getStageRound(), s.getTotalRounds());
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", sessions.getRecords());
        response.put("total", sessions.getTotal());
        response.put("page", page);
        response.put("size", size);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新会话状态。
     * @param sessionId  会话 ID
     * @param status     新状态
     * @return  更新结果
     */
    @PutMapping("/sessions/{sessionId}/status")
    public ResponseEntity<Map<String, Object>> updateSessionStatus(
            @PathVariable String sessionId,
            @RequestParam String status) {
        
        boolean success = conversationService.updateSessionStatus(sessionId, status);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "状态更新成功" : "会话不存在");
        return ResponseEntity.ok(response);
    }

    /**
     * 结束会话。
     * @param sessionId  会话 ID
     * @return  结束结果
     */
    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<Map<String, Object>> endSession(@PathVariable String sessionId) {
        boolean success = conversationService.endSession(sessionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "会话已结束" : "会话不存在");
        return ResponseEntity.ok(response);
    }

    /**
     * 暂停会话（用户退出面试但未完成）。
     * @param sessionId  会话 ID
     * @return  暂停结果
     */
    @PostMapping("/sessions/{sessionId}/pause")
    public ResponseEntity<Map<String, Object>> pauseSession(@PathVariable String sessionId) {
        boolean success = conversationService.pauseSession(sessionId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "会话已暂停" : "会话不存在");
        return ResponseEntity.ok(response);
    }

    /**
     * 删除会话（级联删除消息）。
     * @param sessionId  会话 ID
     * @return  删除结果
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Map<String, Object>> deleteSession(@PathVariable String sessionId) {
        boolean success = conversationService.deleteSession(sessionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "会话删除成功" : "会话不存在");
        return ResponseEntity.ok(response);
    }

    // ==================== 消息管理 ====================

    /**
     * 保存消息。
     * @param message  消息对象
     * @return  保存后的消息
     */
    @PostMapping("/messages")
    public ResponseEntity<Map<String, Object>> saveMessage(@RequestBody InterviewMessage message) {
        InterviewMessage saved = conversationService.saveMessage(message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", saved);
        return ResponseEntity.ok(response);
    }

    /**
     * 批量保存消息。
     * @param messages  消息列表
     * @return  保存结果
     */
    @PostMapping("/messages/batch")
    public ResponseEntity<Map<String, Object>> saveMessages(@RequestBody List<InterviewMessage> messages) {
        boolean success = conversationService.saveMessages(messages);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("count", messages.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 获取会话的所有消息。
     * @param sessionId  会话 ID
     * @return  消息列表
     */
    @GetMapping("/messages/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> getMessagesBySessionId(@PathVariable String sessionId) {
        List<InterviewMessage> messages = conversationService.getMessagesBySessionId(sessionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", messages);
        response.put("count", messages.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 获取单条消息。
     * @param messageId  消息 ID
     * @return  消息对象
     */
    @GetMapping("/messages/{messageId}")
    public ResponseEntity<Map<String, Object>> getMessage(@PathVariable String messageId) {
        InterviewMessage message = conversationService.getMessage(messageId);
        
        Map<String, Object> response = new HashMap<>();
        if (message != null) {
            response.put("success", true);
            response.put("message", message);
        } else {
            response.put("success", false);
            response.put("message", "消息不存在");
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 更新消息内容。
     * @param messageId  消息 ID
     * @param body       请求体，包含 content 和 metadata 字段
     * @return  更新结果
     */
    @PutMapping("/messages/{messageId}")
    public ResponseEntity<Map<String, Object>> updateMessage(
            @PathVariable String messageId,
            @RequestBody Map<String, String> body) {
        
        String content = body.get("content");
        String metadata = body.get("metadata");
        boolean success = conversationService.updateMessage(messageId, content, metadata);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "消息更新成功" : "消息不存在");
        return ResponseEntity.ok(response);
    }

    /**
     * 删除消息。
     * @param messageId  消息 ID
     * @return  删除结果
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Map<String, Object>> deleteMessage(@PathVariable String messageId) {
        boolean success = conversationService.deleteMessage(messageId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "消息删除成功" : "消息不存在");
        return ResponseEntity.ok(response);
    }

    // ==================== 统计查询 ====================

    /**
     * 获取用户的会话数量。
     * @param userId  用户 ID
     * @return  会话数量
     */
    @GetMapping("/stats/user/{userId}/count")
    public ResponseEntity<Map<String, Object>> getSessionCountByUser(@PathVariable String userId) {
        long count = conversationService.countSessionsByUser(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取会话的消息数量。
     * @param sessionId  会话 ID
     * @return  消息数量
     */
    @GetMapping("/stats/session/{sessionId}/count")
    public ResponseEntity<Map<String, Object>> getMessageCountBySession(@PathVariable String sessionId) {
        long count = conversationService.countMessagesBySessionId(sessionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    // ==================== 清理任务 ====================

    /**
     * 手动触发过期会话清理。
     * @param retentionDays  保留天数
     * @return  清理结果
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupExpiredSessions(
            @RequestParam(defaultValue = "30") int retentionDays) {
        
        int deleted = conversationService.cleanupExpiredSessions(retentionDays);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("deletedCount", deleted);
        response.put("message", String.format("清理完成，共删除 %d 个过期会话", deleted));
        return ResponseEntity.ok(response);
    }
}