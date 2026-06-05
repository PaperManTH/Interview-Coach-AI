package com.interviewcoach.tts;

import com.interviewcoach.config.TtsProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * TTS Provider 工厂。
 * 根据配置动态创建对应的 Provider 实例。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TtsProviderFactory {

    private final TtsProperties ttsProperties;
    private TtsProvider provider;

    @PostConstruct
    public void init() {
        String type = ttsProperties.getType().toUpperCase();
        try {
            TtsProviderType providerType = TtsProviderType.valueOf(type);
            provider = createProvider(providerType);
            log.info("[TTS] 初始化 Provider: {}", providerType);
        } catch (IllegalArgumentException e) {
            log.warn("[TTS] 未知的 Provider 类型: {}, 使用 Mock", type);
            provider = new MockTtsProvider();
        }
    }

    private TtsProvider createProvider(TtsProviderType type) {
        return switch (type) {
            case MOCK -> new MockTtsProvider();
            case IFLYTEK -> createIflytekProvider();
            case OPENAI -> createOpenaiProvider();
            case AZURE -> createAzureProvider();
        };
    }

    private TtsProvider createIflytekProvider() {
        log.warn("[TTS] iFlytek Provider 尚未实现，降级为 Mock");
        return new MockTtsProvider();
    }

    private TtsProvider createOpenaiProvider() {
        log.warn("[TTS] OpenAI Provider 尚未实现，降级为 Mock");
        return new MockTtsProvider();
    }

    private TtsProvider createAzureProvider() {
        log.warn("[TTS] Azure Provider 尚未实现，降级为 Mock");
        return new MockTtsProvider();
    }

    public TtsProvider getProvider() {
        return provider;
    }

    public void reload() {
        init();
    }
}
