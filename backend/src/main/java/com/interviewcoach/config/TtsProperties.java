package com.interviewcoach.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TTS Provider 配置。
 */
@Data
@ConfigurationProperties(prefix = "app.provider.tts")
public class TtsProperties {

    private String type = "mock";
    private String apiKey = "";
    private String baseUrl = "";
    private String voice = "default";
    private int timeoutSeconds = 30;
}
