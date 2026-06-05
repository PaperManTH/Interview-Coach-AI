package com.interviewcoach.userconfig;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户 Provider 配置实体。
 * 对应表 user_provider_config。
 */
@Data
@TableName("user_provider_config")
public class UserProviderConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userId;

    // ===== ASR =====
    private String asrType;
    private String asrApiKey;
    private String asrBaseUrl;

    // ===== TTS =====
    private String ttsType;
    private String ttsApiKey;
    private String ttsBaseUrl;
    private String ttsVoice;

    // ===== LLM =====
    private String llmType;
    private String llmApiKey;
    private String llmBaseUrl;
    private String llmModel;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
