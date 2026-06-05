package com.interviewcoach.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * ChatModel 工厂：统一处理所有 OpenAI 兼容 Provider。
 * <p>
 * 国内厂商：deepseek / qwen(阿里百炼) / glm(智谱) / kimi(Moonshot)
 * 国外厂商：openai / azure
 * <p>
 * 全部走 OpenAiApi + OpenAiChatModel，切换只需改配置
 */
@Slf4j
@Configuration
@EnableConfigurationProperties
public class ChatModelConfig {

    private final LlmProperties llmProperties;

    public ChatModelConfig(LlmProperties llmProperties) {
        this.llmProperties = llmProperties;
    }

    @Bean
    public ChatModel chatModel() {
        String provider = llmProperties.getProvider();
        log.info("[LLM] Provider: {}", provider);

        if ("mock".equalsIgnoreCase(provider)) {
            log.info("[LLM] Mock 模式");
            return null;
        }

        ProviderConfig cfg = selectProviderConfig(provider);
        if (cfg == null || isBlank(cfg.getApiKey())) {
            log.info("[LLM] {} Key 未设置，使用 Mock 模式", provider);
            return null;
        }

        String baseUrl = resolveBaseUrl(provider, cfg);
        String model = cfg.getModel();

        log.info("[LLM] 创建 ChatModel: provider={}, model={}, baseUrl={}", provider, model, baseUrl);
        return buildModel(baseUrl, cfg.getApiKey(), model);
    }

    /**
     * 根据 provider 名称选择对应配置。
     */
    private ProviderConfig selectProviderConfig(String provider) {
        Map<String, ProviderConfig> map = Map.of(
                "openai", llmProperties.getOpenai(),
                "azure", llmProperties.getAzure(),
                "deepseek", llmProperties.getDeepseek(),
                "qwen", llmProperties.getQwen(),
                "glm", llmProperties.getGlm(),
                "kimi", llmProperties.getKimi()
        );
        return map.getOrDefault(provider, null);
    }

    /**
     * Azure 需要拼接 deployment 路径，其余直接使用 baseUrl。
     */
    private String resolveBaseUrl(String provider, ProviderConfig cfg) {
        if ("azure".equalsIgnoreCase(provider)) {
            // Azure: endpoint + /openai/deployments/{model}
            return cfg.getBaseUrl().replaceAll("/$", "")
                    + "/openai/deployments/" + cfg.getModel();
        }
        return cfg.getBaseUrl();
    }

    private ChatModel buildModel(String baseUrl, String apiKey, String model) {
        OpenAiApi api = new OpenAiApi(baseUrl, apiKey);
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(model)
                .withTemperature((float) llmProperties.getTemperature())
                .build();
        return new OpenAiChatModel(api, options);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /**
     * 获取 Provider 的默认 BaseUrl。
     * @param provider deepseek / qwen / glm / kimi / openai / azure
     */
    public static String getDefaultBaseUrl(String provider) {
        return switch (provider.toLowerCase()) {
            case "deepseek" -> "https://api.deepseek.com";
            case "qwen" -> "https://dashscope.aliyuncs.com/compatible-mode/v1";
            case "glm" -> "https://open.bigmodel.cn/api/paas/v4";
            case "kimi" -> "https://api.moonshot.cn/v1";
            case "openai" -> "https://api.openai.com";
            default -> "https://api.openai.com";
        };
    }

    // ==================== Configuration Properties ====================

    @Data
    @ConfigurationProperties(prefix = "app.llm")
    public static class LlmProperties {
        private String provider = "mock";
        private double temperature = 0.7;

        @NestedConfigurationProperty
        private ProviderConfig openai = new ProviderConfig("https://api.openai.com", "gpt-4o-mini");

        @NestedConfigurationProperty
        private ProviderConfig azure = new ProviderConfig("", "gpt-4o-mini");

        @NestedConfigurationProperty
        private ProviderConfig deepseek = new ProviderConfig("https://api.deepseek.com", "deepseek-chat");

        @NestedConfigurationProperty
        private ProviderConfig qwen = new ProviderConfig(
                "https://dashscope.aliyuncs.com/compatible-mode/v1", "qwen-plus");

        @NestedConfigurationProperty
        private ProviderConfig glm = new ProviderConfig(
                "https://open.bigmodel.cn/api/paas/v4", "glm-4-flash");

        @NestedConfigurationProperty
        private ProviderConfig kimi = new ProviderConfig(
                "https://api.moonshot.cn/v1", "moonshot-v1-8k");
    }

    @Data
    public static class ProviderConfig {
        private String apiKey;
        private String baseUrl;
        private String model;

        public ProviderConfig() {}

        public ProviderConfig(String baseUrl, String model) {
            this.baseUrl = baseUrl;
            this.model = model;
        }
    }
}