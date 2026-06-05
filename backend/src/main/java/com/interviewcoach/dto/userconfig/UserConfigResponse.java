package com.interviewcoach.dto.userconfig;

import com.interviewcoach.userconfig.UserProviderConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户 Provider 配置响应 DTO。
 * 注意：不返回敏感信息（如 API Key）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConfigResponse {

    private String userId;
    private String asrType;
    private boolean asrConfigured;
    private String asrBaseUrl;

    private String ttsType;
    private boolean ttsConfigured;
    private String ttsBaseUrl;
    private String ttsVoice;

    private String llmType;
    private boolean llmConfigured;
    private String llmBaseUrl;
    private String llmModel;

    /**
     * 从实体转换（隐藏敏感信息）。
     */
    public static UserConfigResponse fromEntity(UserProviderConfig config) {
        if (config == null) {
            return null;
        }
        return UserConfigResponse.builder()
                .userId(config.getUserId())
                .asrType(config.getAsrType())
                .asrConfigured(config.getAsrApiKey() != null && !config.getAsrApiKey().isEmpty())
                .asrBaseUrl(config.getAsrBaseUrl())
                .ttsType(config.getTtsType())
                .ttsConfigured(config.getTtsApiKey() != null && !config.getTtsApiKey().isEmpty())
                .ttsBaseUrl(config.getTtsBaseUrl())
                .ttsVoice(config.getTtsVoice())
                .llmType(config.getLlmType())
                .llmConfigured(config.getLlmApiKey() != null && !config.getLlmApiKey().isEmpty())
                .llmBaseUrl(config.getLlmBaseUrl())
                .llmModel(config.getLlmModel())
                .build();
    }
}
