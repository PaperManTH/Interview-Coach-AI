package com.interviewcoach.entity.dto.userconfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户 Provider 配置请求 DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConfigRequest {

    private String asrType;
    private String asrApiKey;
    private String asrBaseUrl;
    private String asrRegion;

    private String ttsType;
    private String ttsApiKey;
    private String ttsBaseUrl;
    private String ttsVoice;
    private String ttsRegion;

    private String llmType;
    private String llmApiKey;
    private String llmBaseUrl;
    private String llmModel;
    private String llmRegion;
}
