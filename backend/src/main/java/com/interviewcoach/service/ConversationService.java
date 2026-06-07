package com.interviewcoach.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.interviewcoach.entity.InterviewMessage;
import com.interviewcoach.entity.InterviewSession;

import java.util.List;

/**
 * 对话记忆服务接口 - 会话和消息管理。
 */
public interface ConversationService {

    // ===== 会话管理 =====

    InterviewSession createSession(String userId, String interviewType);

    InterviewSession getActiveSession(String userId);

    InterviewSession getSession(String sessionId);

    IPage<InterviewSession> listSessionsByUser(String userId, int page, int size);

    List<InterviewSession> listAllSessionsByUser(String userId);

    boolean updateSessionStatus(String sessionId, String status);

    boolean endSession(String sessionId);

    boolean pauseSession(String sessionId);

    boolean updateStageProgress(String sessionId, String currentStage, Integer stageRound, Integer totalRounds);

    boolean deleteSession(String sessionId);

    int cleanupExpiredSessions(int retentionDays);

    // ===== 消息管理 =====

    InterviewMessage saveMessage(InterviewMessage message);

    boolean saveMessages(List<InterviewMessage> messages);

    List<InterviewMessage> getMessagesBySessionId(String sessionId);

    InterviewMessage getMessage(String messageId);

    boolean updateMessage(String messageId, String content, String metadata);

    boolean deleteMessage(String messageId);

    int deleteMessagesBySessionId(String sessionId);

    // ===== 统计查询 =====

    long countSessionsByUser(String userId);

    long countMessagesBySessionId(String sessionId);
}
