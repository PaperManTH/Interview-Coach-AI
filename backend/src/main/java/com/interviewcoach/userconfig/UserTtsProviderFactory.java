package com.interviewcoach.userconfig;

import com.interviewcoach.tts.TtsProvider;
import com.interviewcoach.config.TtsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户级 TTS Provider 工厂。
 * 根据用户配置动态创建 Provider 实例。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserTtsProviderFactory {

    private final TtsProperties defaultProperties;

    public TtsProvider createProvider(UserProviderConfig userConfig) {
        String type = getProviderType(userConfig);
        
        try {
            return switch (type.toUpperCase()) {
                case "IFLYTEK" -> createIflytekProvider(userConfig);
                case "OPENAI" -> createOpenaiProvider(userConfig);
                case "AZURE" -> createAzureProvider(userConfig);
                default -> createMockProvider();
            };
        } catch (Exception e) {
            log.warn("[TTS] 创建用户 Provider 失败，降级为 Mock: {}", e.getMessage());
            return createMockProvider();
        }
    }

    private String getProviderType(UserProviderConfig userConfig) {
        if (userConfig == null || userConfig.getTtsType() == null || userConfig.getTtsType().isEmpty()) {
            return defaultProperties.getType();
        }
        return userConfig.getTtsType();
    }

    private TtsProvider createIflytekProvider(UserProviderConfig userConfig) {
        log.info("[TTS] 创建 iFlytek Provider (用户配置) userId={}", userConfig.getUserId());
        return createMockProvider();
    }

    private TtsProvider createOpenaiProvider(UserProviderConfig userConfig) {
        log.info("[TTS] 创建 OpenAI Provider (用户配置) userId={}", userConfig.getUserId());
        return createMockProvider();
    }

    private TtsProvider createAzureProvider(UserProviderConfig userConfig) {
        log.info("[TTS] 创建 Azure Provider (用户配置) userId={}", userConfig.getUserId());
        return createMockProvider();
    }

    private TtsProvider createMockProvider() {
        return new com.interviewcoach.tts.MockTtsProvider();
    }
}
