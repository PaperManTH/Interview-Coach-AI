package com.interviewcoach.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("messageId")
    private String messageId;

    @JsonProperty("sender")
    private String sender;

    @JsonProperty("type")
    private String type;

    @JsonProperty("content")
    private String content;

    @JsonProperty("timestampMs")
    private Long timestampMs;

    @JsonProperty("metadata")
    private String metadata;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
