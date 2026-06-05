package com.interviewcoach.userconfig;

import lombok.Data;

/**
 * 用户级 Provider 配置。
 * 存储用户自定义的 API 密钥和配置。
 */
@Data
public class UserProviderConfig {

    private String userId;

    private String asrType;
    private String asrApiKey;
    private String asrBaseUrl;

    private String ttsType;
    private String ttsApiKey;
    private String ttsBaseUrl;
    private String ttsVoice;

    private String llmType;
    private String llmApiKey;
    private String llmBaseUrl;
    private String llmModel;

    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    public UserProviderConfig() {
        this.createdAt = java.time.LocalDateTime.now();
        this.updatedAt = java.time.LocalDateTime.now();
    }
}
