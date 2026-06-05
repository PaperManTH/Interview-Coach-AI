package com.interviewcoach.config.userconfig;

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
    private String asrRegion;

    // ===== TTS =====
    private String ttsType;
    private String ttsApiKey;
    private String ttsBaseUrl;
    private String ttsVoice;
    private String ttsRegion;

    // ===== LLM =====
    private String llmType;
    private String llmApiKey;
    private String llmBaseUrl;
    private String llmModel;
    private String llmRegion;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
