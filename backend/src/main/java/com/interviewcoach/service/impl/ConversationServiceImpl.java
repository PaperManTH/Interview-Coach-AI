package com.interviewcoach.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.interviewcoach.common.Constants;
import com.interviewcoach.common.InterviewConstants;
import com.interviewcoach.entity.InterviewMessage;
import com.interviewcoach.entity.InterviewSession;
import com.interviewcoach.mapper.InterviewMessageMapper;
import com.interviewcoach.mapper.InterviewSessionMapper;
import com.interviewcoach.service.ConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 对话记忆服务实现 - 提供完整的会话和消息管理功能
 */
@Slf4j
@Service
public class ConversationServiceImpl implements ConversationService {

    @Autowired
    private InterviewSessionMapper sessionMapper;

    @Autowired
    private InterviewMessageMapper messageMapper;

    // ===== 会话管理 =====

    /**
     * 创建新会话
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public InterviewSession createSession(String userId, String interviewType) {
        InterviewSession session = new InterviewSession();
        session.setSessionId(UUID.randomUUID().toString().replace("-", ""));
        session.setUserId(userId != null ? userId : Constants.DEFAULT_USER_ID);
        session.setStatus(Constants.SESSION_STATUS_ACTIVE);
        session.setInterviewType(interviewType != null ? interviewType : Constants.INTERVIEW_TYPE_GENERAL);
        session.setStartedAt(LocalDateTime.now());

        // 初始化动态面试进度
        if (Constants.INTERVIEW_TYPE_DYNAMIC.equalsIgnoreCase(interviewType)) {
            session.setCurrentStage(InterviewConstants.STAGE_WARMUP);
            session.setStageRound(0);
            session.setTotalRounds(0);
        }

        int result = sessionMapper.insert(session);
        if (result > 0) {
            log.info("[Conversation] 会话创建成功: sessionId={}, userId={}", session.getSessionId(), userId);
        }
        return session;
    }

    /**
     * 获取用户的活跃会话
     */
    @Override
    public InterviewSession getActiveSession(String userId) {
        return sessionMapper.selectOne(
                new LambdaQueryWrapper<InterviewSession>()
                        .eq(InterviewSession::getUserId, userId)
                        .in(InterviewSession::getStatus, 
                            (Object[]) Constants.SESSION_STATUS_ACTIVE_OR_PAUSED)
                        .orderByDesc(InterviewSession::getStartedAt)
                        .last("LIMIT 1")
        );
    }

    /**
     * 根据会话ID获取会话
     */
    @Override
    public InterviewSession getSession(String sessionId) {
        return sessionMapper.selectOne(
                new LambdaQueryWrapper<InterviewSession>()
                        .eq(InterviewSession::getSessionId, sessionId)
        );
    }

    /**
     * 根据用户ID分页获取会话列表
     */
    @Override
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
    @Override
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
    @Override
    public boolean updateSessionStatus(String sessionId, String status) {
        InterviewSession session = getSession(sessionId);
        if (session == null) {
            log.warn("[Conversation] 会话不存在: sessionId={}", sessionId);
            return false;
        }
        session.setStatus(status);
        if (Constants.SESSION_STATUS_ENDED.equals(status)) {
            session.setEndedAt(LocalDateTime.now());
        }
        int result = sessionMapper.updateById(session);
        return result > 0;
    }

    /**
     * 结束会话
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean endSession(String sessionId) {
        return updateSessionStatus(sessionId, Constants.SESSION_STATUS_ENDED);
    }

    /**
     * 暂停会话（用户退出但未结束面试）
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean pauseSession(String sessionId) {
        InterviewSession session = getSession(sessionId);
        if (session == null) {
            log.warn("[Conversation] 会话不存在: sessionId={}", sessionId);
            return false;
        }
        session.setStatus(Constants.SESSION_STATUS_PAUSED);
        session.setPausedAt(LocalDateTime.now());
        int result = sessionMapper.updateById(session);
        if (result > 0) {
            log.info("[Conversation] 会话已暂停: sessionId={}", sessionId);
        }
        return result > 0;
    }

    /**
     * 更新会话的阶段进度
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateStageProgress(String sessionId, String currentStage, Integer stageRound, Integer totalRounds) {
        InterviewSession session = getSession(sessionId);
        if (session == null) {
            log.warn("[Conversation] 会话不存在: sessionId={}", sessionId);
            return false;
        }
        if (currentStage != null) {
            session.setCurrentStage(currentStage);
        }
        if (stageRound != null) {
            session.setStageRound(stageRound);
        }
        if (totalRounds != null) {
            session.setTotalRounds(totalRounds);
        }
        int result = sessionMapper.updateById(session);
        if (result > 0) {
            log.info("[Conversation] 阶段进度已更新: sessionId={}, stage={}, round={}, total={}",
                    sessionId, currentStage, stageRound, totalRounds);
        }
        return result > 0;
    }

    /**
     * 删除会话（级联删除消息）
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
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
     * 清理过期会话
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int cleanupExpiredSessions(int retentionDays) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);

        // 查找过期会话
        List<InterviewSession> expiredSessions = sessionMapper.selectList(
                new LambdaQueryWrapper<InterviewSession>()
                        .eq(InterviewSession::getStatus, Constants.SESSION_STATUS_ENDED)
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public long countSessionsByUser(String userId) {
        return sessionMapper.selectCount(
                new LambdaQueryWrapper<InterviewSession>()
                        .eq(InterviewSession::getUserId, userId)
        );
    }

    /**
     * 获取会话的消息数量
     */
    @Override
    public long countMessagesBySessionId(String sessionId) {
        return messageMapper.selectCount(
                new LambdaQueryWrapper<InterviewMessage>()
                        .eq(InterviewMessage::getSessionId, sessionId)
        );
    }
}
