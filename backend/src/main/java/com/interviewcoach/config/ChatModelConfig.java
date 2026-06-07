package com.interviewcoach.config;

import com.interviewcoach.common.Constants;
import com.interviewcoach.common.InterviewConstants;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChatModel 配置 + 提示词管理
 */
@Slf4j
@Configuration
public class ChatModelConfig {

    private static final String PROMPTS_DIR = "prompts";

    /** 缓存已加载的提示词 */
    private final Map<String, String> promptCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        loadPrompt("system", "system-default.txt");
        loadPrompt(InterviewConstants.STAGE_WARMUP, "stage-warmup.txt");
        loadPrompt(InterviewConstants.STAGE_TECHNICAL, "stage-technical.txt");
        loadPrompt(InterviewConstants.STAGE_PRESSURE, "stage-pressure.txt");
        log.info("[Config] 提示词加载完成");
    }

    private void loadPrompt(String key, String fileName) {
        String path = PROMPTS_DIR + "/" + fileName;
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (resource.exists()) {
                String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                promptCache.put(key, content.trim());
                log.debug("[Config] 已加载提示词: {}", path);
            } else {
                log.warn("[Config] 提示词文件不存在: {}", path);
            }
        } catch (IOException e) {
            log.error("[Config] 加载提示词失败: {}, error={}", path, e.getMessage());
        }
    }

    // ===== 提示词获取 =====

    public String getSystemPrompt() {
        return promptCache.getOrDefault("system", "你是一个专业的面试教练助手。");
    }

    public String getWarmupPrompt() {
        return promptCache.getOrDefault("warmup", "");
    }

    public String getTechnicalPrompt() {
        return promptCache.getOrDefault("technical", "");
    }

    public String getPressurePrompt() {
        return promptCache.getOrDefault("pressure", "");
    }

    public String getPromptByStage(String stage) {
        return switch (stage) {
            case InterviewConstants.STAGE_WARMUP -> getWarmupPrompt();
            case InterviewConstants.STAGE_TECHNICAL -> getTechnicalPrompt();
            case InterviewConstants.STAGE_PRESSURE -> getPressurePrompt();
            default -> getSystemPrompt();
        };
    }

    // ===== Provider BaseUrl =====

    public static String getDefaultBaseUrl(String provider) {
        if (provider == null) return "https://api.openai.com";
        
        return switch (provider.toLowerCase()) {
            case "deepseek" -> "https://api.deepseek.com";
            case Constants.PROVIDER_QIANWEN -> "https://dashscope.aliyuncs.com/compatible-mode/v1";
            case "glm" -> "https://open.bigmodel.cn/api/paas/v4";
            case "kimi" -> "https://api.moonshot.cn/v1";
            case Constants.PROVIDER_OPENAI -> "https://api.openai.com";
            case Constants.PROVIDER_AZURE -> "https://{your-resource}.openai.azure.com";
            default -> "https://api.openai.com";
        };
    }
}
