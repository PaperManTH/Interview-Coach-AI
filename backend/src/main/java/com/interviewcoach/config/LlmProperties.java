package com.interviewcoach.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * LLM Provider 配置。
 * 具体供应商通过 provider.type 选择，后续由 ProviderFactory 路由到对应实现。
 */
@Data
@ConfigurationProperties(prefix = "app.provider.llm")
public class LlmProperties {

    /** 供应商类型：mock / openai / ... */
    private String type = "mock";

    private String apiKey = "";
    private String baseUrl = "https://api.openai.com";
    private String model = "gpt-4o-mini";
    private int timeoutSeconds = 30;
}
