package com.interviewcoach.llm;

import com.interviewcoach.config.LlmProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * LLM Provider 工厂。
 * 根据配置动态创建对应的 Provider 实例。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LlmProviderFactory {

    private final LlmProperties llmProperties;
    private LlmProvider provider;

    @PostConstruct
    public void init() {
        String type = llmProperties.getType().toUpperCase();
        try {
            LlmProviderType providerType = LlmProviderType.valueOf(type);
            provider = createProvider(providerType);
            log.info("[LLM] 初始化 Provider: {}", providerType);
        } catch (IllegalArgumentException e) {
            log.warn("[LLM] 未知的 Provider 类型: {}, 使用 Mock", type);
            provider = new MockLlmProvider();
        }
    }

    private LlmProvider createProvider(LlmProviderType type) {
        return switch (type) {
            case MOCK -> new MockLlmProvider();
            case OPENAI -> createOpenaiProvider();
            case AZURE -> createAzureProvider();
            case QIANWEN -> createQianwenProvider();
            case DOUBAO -> createDoubaoProvider();
        };
    }

    private LlmProvider createOpenaiProvider() {
        log.warn("[LLM] OpenAI Provider 尚未实现，降级为 Mock");
        return new MockLlmProvider();
    }

    private LlmProvider createAzureProvider() {
        log.warn("[LLM] Azure Provider 尚未实现，降级为 Mock");
        return new MockLlmProvider();
    }

    private LlmProvider createQianwenProvider() {
        log.warn("[LLM] 千问 Provider 尚未实现，降级为 Mock");
        return new MockLlmProvider();
    }

    private LlmProvider createDoubaoProvider() {
        log.warn("[LLM] 豆包 Provider 尚未实现，降级为 Mock");
        return new MockLlmProvider();
    }

    public LlmProvider getProvider() {
        return provider;
    }

    public void reload() {
        init();
    }
}
