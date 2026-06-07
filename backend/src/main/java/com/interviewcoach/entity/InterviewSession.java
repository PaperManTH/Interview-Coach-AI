package com.interviewcoach.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 面试会话实体。
 * 对应表 interview_session。
 */
@Data
@TableName("interview_session")
public class InterviewSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionId;
    private String userId;
    private String status;
    private String interviewType;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime pausedAt; // 暂停时间（用户退出但未结束面试时记录）
    
    /** 当前面试阶段（warmup / technical / pressure），用于恢复会话时定位进度 */
    private String currentStage;
    
    /** 当前阶段内的轮次（0开始），用于恢复会话时定位进度 */
    private Integer stageRound;
    
    /** 总轮次，用于进度展示 */
    private Integer totalRounds;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
