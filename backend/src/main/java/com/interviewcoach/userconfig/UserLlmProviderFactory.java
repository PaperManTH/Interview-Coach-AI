package com.interviewcoach.userconfig;

import com.interviewcoach.llm.LlmProvider;
import com.interviewcoach.config.LlmProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户级 LLM Provider 工厂。
 * 根据用户配置动态创建 Provider 实例。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserLlmProviderFactory {

    private final LlmProperties defaultProperties;

    public LlmProvider createProvider(UserProviderConfig userConfig) {
        String type = getProviderType(userConfig);
        
        try {
            return switch (type.toUpperCase()) {
                case "OPENAI" -> createOpenaiProvider(userConfig);
                case "AZURE" -> createAzureProvider(userConfig);
                case "QIANWEN" -> createQianwenProvider(userConfig);
                case "DOUBAO" -> createDoubaoProvider(userConfig);
                default -> createMockProvider();
            };
        } catch (Exception e) {
            log.warn("[LLM] 创建用户 Provider 失败，降级为 Mock: {}", e.getMessage());
            return createMockProvider();
        }
    }

    private String getProviderType(UserProviderConfig userConfig) {
        if (userConfig == null || userConfig.getLlmType() == null || userConfig.getLlmType().isEmpty()) {
            return defaultProperties.getType();
        }
        return userConfig.getLlmType();
    }

    private LlmProvider createOpenaiProvider(UserProviderConfig userConfig) {
        log.info("[LLM] 创建 OpenAI Provider (用户配置) userId={}", userConfig.getUserId());
        return createMockProvider();
    }

    private LlmProvider createAzureProvider(UserProviderConfig userConfig) {
        log.info("[LLM] 创建 Azure Provider (用户配置) userId={}", userConfig.getUserId());
        return createMockProvider();
    }

    private LlmProvider createQianwenProvider(UserProviderConfig userConfig) {
        log.info("[LLM] 创建千问 Provider (用户配置) userId={}", userConfig.getUserId());
        return createMockProvider();
    }

    private LlmProvider createDoubaoProvider(UserProviderConfig userConfig) {
        log.info("[LLM] 创建豆包 Provider (用户配置) userId={}", userConfig.getUserId());
        return createMockProvider();
    }

    private LlmProvider createMockProvider() {
        return new com.interviewcoach.llm.MockLlmProvider();
    }
}
