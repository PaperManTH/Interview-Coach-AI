package com.interviewcoach.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.interviewcoach.entity.InterviewMessage;
import com.interviewcoach.entity.InterviewSession;
import com.interviewcoach.mapper.InterviewMessageMapper;
import com.interviewcoach.mapper.InterviewSessionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 对话记忆服务 - 提供完整的会话和消息管理功能
 * 
 * 功能包括：
 * - 会话的创建、查询、更新、删除
 * - 消息的写入、读取、删除
 * - 会话过期清理机制
 * - 事务管理确保数据一致性
 */
@Slf4j
@Service
public class ConversationService {

    @Autowired
    private InterviewSessionMapper sessionMapper;

    @Autowired
    private InterviewMessageMapper messageMapper;

    // ===== 会话管理 =====

    /**
     * 创建新会话
     * @param userId 用户ID
     * @param interviewType 面试类型（hr/technical/pressure/dynamic）
     * @return 创建的会话实体
     */
    @Transactional(rollbackFor = Exception.class)
    public InterviewSession createSession(String userId, String interviewType) {
        InterviewSession session = new InterviewSession();
        session.setSessionId(UUID.randomUUID().toString().replace("-", ""));
        session.setUserId(userId != null ? userId : "anonymous");
        session.setStatus("active");
        session.setInterviewType(interviewType != null ? interviewType : "general");
        session.setStartedAt(LocalDateTime.now());

        int result = sessionMapper.insert(session);
        if (result > 0) {
            log.info("[Conversation] 会话创建成功: sessionId={}, userId={}", session.getSessionId(), userId);
        }
        return session;
    }

    /**
     * 获取用户的活跃会话（状态为 active 的会话）
     * @param userId 用户ID
     * @return 活跃会话，如果没有则返回 null
     */
    public InterviewSession getActiveSession(String userId) {
        return sessionMapper.selectOne(
                new LambdaQueryWrapper<InterviewSession>()
                        .eq(InterviewSession::getUserId, userId)
                        .eq(InterviewSession::getStatus, "active")
                        .orderByDesc(InterviewSession::getStartedAt)
                        .last("LIMIT 1")
        );
    }

    /**
     * 根据会话ID获取会话
     */
    public InterviewSession getSession(String sessionId) {
        return sessionMapper.selectOne(
                new LambdaQueryWrapper<InterviewSession>()
                        .eq(InterviewSession::getSessionId, sessionId)
        );
    }

    /**
     * 根据用户ID分页获取会话列表
     */
    public IPage<InterviewSession> listSessionsByUser(String userId, int page, int size) {
        Page<InterviewSession> pageParam = new Page<>(page, size);
        return sessionMapper.selectPage(pageParam,
                new LambdaQueryWrapper<InterviewSession>()
                        .eq(InterviewSession::getUserId, userId)
                        .orderByDesc(InterviewSession::getStartedAt)
        );
    }

    /**
     * 获取用户的会话列表（不分页）
     */
    public List<InterviewSession> listAllSessionsByUser(String userId) {
        return sessionMapper.selectList(
                new LambdaQueryWrapper<InterviewSession>()
                        .eq(InterviewSession::getUserId, userId)
                        .orderByDesc(InterviewSession::getStartedAt)
        );
    }

    /**
     * 更新会话状态
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSessionStatus(String sessionId, String status) {
        InterviewSession session = getSession(sessionId);
        if (session == null) {
            log.warn("[Conversation] 会话不存在: sessionId={}", sessionId);
            return false;
        }
        session.setStatus(status);
        if ("ended".equals(status)) {
            session.setEndedAt(LocalDateTime.now());
        }
        int result = sessionMapper.updateById(session);
        return result > 0;
    }

    /**
     * 结束会话
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean endSession(String sessionId) {
        return updateSessionStatus(sessionId, "ended");
    }

    /**
     * 暂停会话（用户退出但未结束面试）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean pauseSession(String sessionId) {
        InterviewSession session = getSession(sessionId);
        if (session == null) {
            log.warn("[Conversation] 会话不存在: sessionId={}", sessionId);
            return false;
        }
        session.setPausedAt(LocalDateTime.now());
        int result = sessionMapper.updateById(session);
        if (result > 0) {
            log.info("[Conversation] 会话已暂停: sessionId={}", sessionId);
        }
        return result > 0;
    }

    /**
     * 删除会话（级联删除消息）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSession(String sessionId) {
        // 先删除相关消息
        messageMapper.delete(
                new LambdaQueryWrapper<InterviewMessage>()
                        .eq(InterviewMessage::getSessionId, sessionId)
        );
        // 再删除会话
        int result = sessionMapper.delete(
                new LambdaQueryWrapper<InterviewSession>()
                        .eq(InterviewSession::getSessionId, sessionId)
        );
        if (result > 0) {
            log.info("[Conversation] 会话删除成功: sessionId={}", sessionId);
        }
        return result > 0;
    }

    /**
     * 清理过期会话（状态为ended且超过保留时间）
     * @param retentionDays 保留天数
     * @return 删除的会话数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int cleanupExpiredSessions(int retentionDays) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
        
        // 查找过期会话
        List<InterviewSession> expiredSessions = sessionMapper.selectList(
                new LambdaQueryWrapper<InterviewSession>()
                        .eq(InterviewSession::getStatus, "ended")
                        .lt(InterviewSession::getEndedAt, cutoffTime)
        );

        int deletedCount = 0;
        for (InterviewSession session : expiredSessions) {
            if (deleteSession(session.getSessionId())) {
                deletedCount++;
            }
        }
        
        if (deletedCount > 0) {
            log.info("[Conversation] 清理过期会话完成: 删除 {} 个会话", deletedCount);
        }
        return deletedCount;
    }

    // ===== 消息管理 =====

    /**
     * 保存单条消息
     */
    @Transactional(rollbackFor = Exception.class)
    public InterviewMessage saveMessage(InterviewMessage message) {
        if (message.getMessageId() == null || message.getMessageId().isEmpty()) {
            message.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        }
        if (message.getTimestampMs() == null) {
            message.setTimestampMs(System.currentTimeMillis());
        }

        log.info("[Conversation] 正在保存消息: sessionId={}, messageId={}, sender={}, contentLength={}",
                message.getSessionId(), message.getMessageId(), message.getSender(),
                message.getContent() != null ? message.getContent().length() : 0);

        try {
            int result = messageMapper.insert(message);
            if (result > 0) {
                log.info("[Conversation] ✅ 消息保存成功: id={}, messageId={}, sessionId={}",
                        message.getId(), message.getMessageId(), message.getSessionId());
            } else {
                log.warn("[Conversation] ❌ 消息保存失败: messageMapper.insert 返回 0, messageId={}", message.getMessageId());
            }
        } catch (Exception e) {
            log.error("[Conversation] ❌ 消息保存异常: messageId={}, sessionId={}", message.getMessageId(), message.getSessionId(), e);
            throw e;
        }
        return message;
    }

    /**
     * 批量保存消息
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveMessages(List<InterviewMessage> messages) {
        for (InterviewMessage message : messages) {
            if (message.getMessageId() == null || message.getMessageId().isEmpty()) {
                message.setMessageId(UUID.randomUUID().toString().replace("-", ""));
            }
            if (message.getTimestampMs() == null) {
                message.setTimestampMs(System.currentTimeMillis());
            }
        }
        messages.forEach(messageMapper::insert);
        log.info("[Conversation] 批量保存消息: {} 条", messages.size());
        return true;
    }

    /**
     * 根据会话ID获取所有消息（按时间排序）
     */
    public List<InterviewMessage> getMessagesBySessionId(String sessionId) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<InterviewMessage>()
                        .eq(InterviewMessage::getSessionId, sessionId)
                        .orderByAsc(InterviewMessage::getTimestampMs)
        );
    }

    /**
     * 根据消息ID获取消息
     */
    public InterviewMessage getMessage(String messageId) {
        return messageMapper.selectOne(
                new LambdaQueryWrapper<InterviewMessage>()
                        .eq(InterviewMessage::getMessageId, messageId)
        );
    }

    /**
     * 更新消息内容
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMessage(String messageId, String content, String metadata) {
        InterviewMessage message = getMessage(messageId);
        if (message == null) {
            log.warn("[Conversation] 消息不存在: messageId={}", messageId);
            return false;
        }
        if (content != null) {
            message.setContent(content);
        }
        if (metadata != null) {
            message.setMetadata(metadata);
        }
        int result = messageMapper.updateById(message);
        return result > 0;
    }

    /**
     * 删除消息
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMessage(String messageId) {
        int result = messageMapper.delete(
                new LambdaQueryWrapper<InterviewMessage>()
                        .eq(InterviewMessage::getMessageId, messageId)
        );
        return result > 0;
    }

    /**
     * 删除会话的所有消息
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteMessagesBySessionId(String sessionId) {
        return messageMapper.delete(
                new LambdaQueryWrapper<InterviewMessage>()
                        .eq(InterviewMessage::getSessionId, sessionId)
        );
    }

    // ===== 统计查询 =====

    /**
     * 获取用户的会话数量
     */
    public long countSessionsByUser(String userId) {
        return sessionMapper.selectCount(
                new LambdaQueryWrapper<InterviewSession>()
                        .eq(InterviewSession::getUserId, userId)
        );
    }

    /**
     * 获取会话的消息数量
     */
    public long countMessagesBySessionId(String sessionId) {
        return messageMapper.selectCount(
                new LambdaQueryWrapper<InterviewMessage>()
                        .eq(InterviewMessage::getSessionId, sessionId)
        );
    }
}