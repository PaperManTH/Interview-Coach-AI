package com.interviewcoach.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话消息实体。
 * 对应表 interview_message。
 */
@Data
@TableName("interview_message")
public class InterviewMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionId;
    private String messageId;
    private String sender;
    private String type;
    private String content;
    private Long timestampMs;
    private String metadata;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
