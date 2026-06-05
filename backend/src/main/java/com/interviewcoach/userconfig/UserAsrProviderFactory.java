package com.interviewcoach.userconfig;

import com.interviewcoach.asr.AsrContext;
import com.interviewcoach.asr.AsrProvider;
import com.interviewcoach.config.AsrProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户级 ASR Provider 工厂。
 * 根据用户配置动态创建 Provider 实例。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserAsrProviderFactory {

    private final AsrProperties defaultProperties;

    /**
     * 根据用户配置创建 ASR Provider。
     */
    public AsrProvider createProvider(UserProviderConfig userConfig) {
        String type = getProviderType(userConfig);
        
        try {
            return switch (type.toUpperCase()) {
                case "IFLYTEK" -> createIflytekProvider(userConfig);
                case "OPENAI" -> createOpenaiProvider(userConfig);
                case "AZURE" -> createAzureProvider(userConfig);
                default -> createMockProvider();
            };
        } catch (Exception e) {
            log.warn("[ASR] 创建用户 Provider 失败，降级为 Mock: {}", e.getMessage());
            return createMockProvider();
        }
    }

    private String getProviderType(UserProviderConfig userConfig) {
        if (userConfig == null || userConfig.getAsrType() == null || userConfig.getAsrType().isEmpty()) {
            return defaultProperties.getType();
        }
        return userConfig.getAsrType();
    }

    private AsrProvider createIflytekProvider(UserProviderConfig userConfig) {
        log.info("[ASR] 创建 iFlytek Provider (用户配置) userId={}", userConfig.getUserId());
        // 预留：创建 iFlytek Provider 并设置用户的 API Key
        return createMockProvider();
    }

    private AsrProvider createOpenaiProvider(UserProviderConfig userConfig) {
        log.info("[ASR] 创建 OpenAI Provider (用户配置) userId={}", userConfig.getUserId());
        // 预留：创建 OpenAI Provider 并设置用户的 API Key
        return createMockProvider();
    }

    private AsrProvider createAzureProvider(UserProviderConfig userConfig) {
        log.info("[ASR] 创建 Azure Provider (用户配置) userId={}", userConfig.getUserId());
        // 预留：创建 Azure Provider 并设置用户的 API Key
        return createMockProvider();
    }

    private AsrProvider createMockProvider() {
        return new com.interviewcoach.asr.MockAsrProvider();
    }
}
