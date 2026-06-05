package com.interviewcoach.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * TTS 语音合成配置。
 * LLM Key 与 TTS Key 完全独立，互不影响。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.tts")
public class TtsProperties {

    /** 当前 Provider：mock / openai / azure */
    private String provider = "mock";

    @NestedConfigurationProperty
    private TtsProviderConfig openai = new TtsProviderConfig("https://api.openai.com", "tts-1", "alloy");

    @NestedConfigurationProperty
    private TtsProviderConfig azure = new TtsProviderConfig("", "", "zh-CN-XiaoxiaoNeural");

    @Data
    public static class TtsProviderConfig {
        private String apiKey;
        private String baseUrl;
        private String model;
        private String voice;
        /** Azure 专用：region */
        private String region;

        public TtsProviderConfig() {}

        public TtsProviderConfig(String baseUrl, String model, String voice) {
            this.baseUrl = baseUrl;
            this.model = model;
            this.voice = voice;
        }
    }
}