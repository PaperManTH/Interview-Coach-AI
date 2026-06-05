package com.interviewcoach.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ASR Provider 配置。
 */
@Data
@ConfigurationProperties(prefix = "app.provider.asr")
public class AsrProperties {

    private String type = "mock";
    private String apiKey = "";
    private String baseUrl = "";
    private int timeoutSeconds = 30;
}
