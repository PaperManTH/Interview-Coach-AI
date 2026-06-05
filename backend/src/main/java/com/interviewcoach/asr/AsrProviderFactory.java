package com.interviewcoach.asr;

import com.interviewcoach.config.AsrProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ASR Provider 工厂。
 * 根据配置动态创建对应的 Provider 实例。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AsrProviderFactory {

    private final AsrProperties asrProperties;
    private AsrProvider provider;

    @PostConstruct
    public void init() {
        String type = asrProperties.getType().toUpperCase();
        try {
            AsrProviderType providerType = AsrProviderType.valueOf(type);
            provider = createProvider(providerType);
            log.info("[ASR] 初始化 Provider: {}", providerType);
        } catch (IllegalArgumentException e) {
            log.warn("[ASR] 未知的 Provider 类型: {}, 使用 Mock", type);
            provider = new MockAsrProvider();
        }
    }

    private AsrProvider createProvider(AsrProviderType type) {
        return switch (type) {
            case MOCK -> new MockAsrProvider();
            case IFLYTEK -> createIflytekProvider();
            case OPENAI -> createOpenaiProvider();
            case AZURE -> createAzureProvider();
        };
    }

    private AsrProvider createIflytekProvider() {
        // 预留接口，不实现具体逻辑
        log.warn("[ASR] iFlytek Provider 尚未实现，降级为 Mock");
        return new MockAsrProvider();
    }

    private AsrProvider createOpenaiProvider() {
        log.warn("[ASR] OpenAI Provider 尚未实现，降级为 Mock");
        return new MockAsrProvider();
    }

    private AsrProvider createAzureProvider() {
        log.warn("[ASR] Azure Provider 尚未实现，降级为 Mock");
        return new MockAsrProvider();
    }

    public AsrProvider getProvider() {
        return provider;
    }

    public void reload() {
        init();
    }
}
