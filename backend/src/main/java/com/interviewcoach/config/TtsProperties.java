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
    
    private IflytekConfig iflytek = new IflytekConfig();
    private AzureConfig azure = new AzureConfig();
    
    @Data
    public static class IflytekConfig {
        private String apiKey = "";
        private String apiSecret = "";
        private String appId = "";
        private String baseUrl = "";
    }
    
    @Data
    public static class AzureConfig {
        private String apiKey = "";
        private String region = "eastus";
        private String baseUrl = "";
    }
}
