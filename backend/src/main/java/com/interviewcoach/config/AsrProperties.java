package com.interviewcoach.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * ASR 语音识别配置。
 * LLM Key 与 ASR Key 完全独立，互不影响。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.asr")
public class AsrProperties {

    /** 当前 Provider：mock / openai / azure */
    private String provider = "mock";

    @NestedConfigurationProperty
    private AsrProviderConfig openai = new AsrProviderConfig("https://api.openai.com", "whisper-1");

    @NestedConfigurationProperty
    private AsrProviderConfig azure = new AsrProviderConfig("", "");

    @Data
    public static class AsrProviderConfig {
        private String apiKey;
        private String baseUrl;
        private String model;
        /** Azure 专用：region */
        private String region;

        public AsrProviderConfig() {}

        public AsrProviderConfig(String baseUrl, String model) {
            this.baseUrl = baseUrl;
            this.model = model;
        }
    }
}