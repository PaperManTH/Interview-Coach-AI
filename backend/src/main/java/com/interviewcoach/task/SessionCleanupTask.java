package com.interviewcoach.task;

import com.interviewcoach.service.ConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 会话清理定时任务
 * 
 * 定期清理已结束且超过保留期限的会话及其消息，防止数据库膨胀
 */
@Slf4j
@Component
public class SessionCleanupTask {

    @Autowired
    private ConversationService conversationService;

    @Value("${interview.conversation.retention-days:30}")
    private int retentionDays;

    /**
     * 每天凌晨 3:00 执行过期会话清理
     * 
     * cron 表达式: 秒 分 时 日 月 周
     * "0 0 3 * * ?" = 每天凌晨3点执行
     */
    @Scheduled(cron = "${interview.conversation.cleanup-cron:0 0 3 * * ?}")
    public void cleanupExpiredSessions() {
        log.info("[Cleanup] 开始执行会话清理任务，保留天数: {}", retentionDays);
        try {
            int deletedCount = conversationService.cleanupExpiredSessions(retentionDays);
            if (deletedCount > 0) {
                log.info("[Cleanup] 会话清理完成，共删除 {} 个过期会话", deletedCount);
            } else {
                log.debug("[Cleanup] 没有需要清理的过期会话");
            }
        } catch (Exception e) {
            log.error("[Cleanup] 会话清理任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 每周日凌晨 2:00 执行深度清理（可选）
     * 可以根据需要调整保留策略
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void weeklyDeepCleanup() {
        // 周日执行更激进的清理策略（保留15天）
        int aggressiveRetention = Math.min(retentionDays, 15);
        log.info("[Cleanup] 执行周度深度清理任务，保留天数: {}", aggressiveRetention);
        try {
            int deletedCount = conversationService.cleanupExpiredSessions(aggressiveRetention);
            log.info("[Cleanup] 周度深度清理完成，共删除 {} 个过期会话", deletedCount);
        } catch (Exception e) {
            log.error("[Cleanup] 周度深度清理任务执行失败: {}", e.getMessage(), e);
        }
    }
}