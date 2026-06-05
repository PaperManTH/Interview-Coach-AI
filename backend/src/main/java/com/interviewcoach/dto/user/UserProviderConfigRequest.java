package com.interviewcoach.dto.user;

import lombok.Data;

/**
 * 用户 Provider 配置请求 DTO。
 */
@Data
public class UserProviderConfigRequest {

    private String asrProviderType;
    private String asrApiKey;
    private String asrBaseUrl;

    private String ttsProviderType;
    private String ttsApiKey;
    private String ttsBaseUrl;
    private String ttsVoice;

    private String llmProviderType;
    private String llmApiKey;
    private String llmBaseUrl;
    private String llmModel;
}
