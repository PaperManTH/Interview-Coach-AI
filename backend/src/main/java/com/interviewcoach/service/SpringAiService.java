package com.interviewcoach.service;

import com.interviewcoach.config.ChatModelConfig;
import com.interviewcoach.config.userconfig.UserProviderConfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 服务 - 通过 Spring AI ChatModel 调用 LLM。
 * 优先使用用户个人配置，没配则默认 Mock。
 */
@Slf4j
@Service
public class SpringAiService {

    @Autowired(required = false)
    private ChatModel chatModel;

    @Autowired
    private UserConfigService userConfigService;

    /** 缓存用户 ChatModel，避免每次请求都创建 */
    private final ConcurrentHashMap<String, ChatModel> userModelCache = new ConcurrentHashMap<>();

    private static final String SYSTEM_PROMPT =
            "你是一个专业的面试教练助手，帮助用户准备面试，回答面试相关问题。语言用中文。";

    public String chat(String message) {
        return internalChat(message, SYSTEM_PROMPT);
    }

    /**
     * 使用用户个人配置的 ChatModel
     */
    public String chat(String userId, String message) {
        return internalChatWithUser(userId, message, SYSTEM_PROMPT);
    }

    public String chatWithSystem(String systemMessage, String userMessage) {
        return internalChat(userMessage, systemMessage);
    }

    public String chatWithTemplate(String template, Map<String, Object> params) {
        String message = template;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue().toString());
        }
        return chat(message);
    }

    private String internalChat(String userMessage, String systemMessage) {
        return doChat(null, userMessage, systemMessage);
    }

    private String internalChatWithUser(String userId, String userMessage, String systemMessage) {
        return doChat(userId, userMessage, systemMessage);
    }

    private String doChat(String userId, String userMessage, String systemMessage) {
        log.info("[AI] userId={}, query={}", userId,
                userMessage.substring(0, Math.min(userMessage.length(), 50)));

        ChatModel model = resolveChatModel(userId);
        if (model == null) {
            return mock(userMessage);
        }

        try {
            List<Message> messages = List.of(
                    new SystemMessage(systemMessage),
                    new UserMessage(userMessage)
            );
            Prompt prompt = new Prompt(messages);
            String content = model.call(prompt).getResult().getOutput().getContent();

            log.info("[AI] 响应长度: {} chars", content.length());
            return content;
        } catch (Exception e) {
            log.error("[AI] ChatModel 调用失败: {}", e.getMessage());
            return "抱歉，AI 服务暂时不可用：" + e.getMessage();
        }
    }

    /**
     * 解析 ChatModel 的优先级：
     * 1. 用户个人配置（从 DB 读取）→ 缓存
     * 2. 全局 ChatModel（从 application.yml）
     * 3. null → Mock
     */
    private ChatModel resolveChatModel(String userId) {
        if (userId != null) {
            ChatModel cached = userModelCache.get(userId);
            if (cached != null) return cached;

            ChatModel userModel = buildUserChatModel(userId);
            if (userModel != null) {
                userModelCache.put(userId, userModel);
                return userModel;
            }
        }
        return chatModel; // null = Mock
    }

    private ChatModel buildUserChatModel(String userId) {
        UserProviderConfig config = userConfigService.getConfig(userId);
        if (config == null || isBlank(config.getLlmType()) || "mock".equalsIgnoreCase(config.getLlmType())) {
            return null;
        }
        if (isBlank(config.getLlmApiKey())) return null;

        String baseUrl = config.getLlmBaseUrl();
        if (isBlank(baseUrl)) {
            baseUrl = ChatModelConfig.getDefaultBaseUrl(config.getLlmType());
        }
        String model = config.getLlmModel();
        if (isBlank(model)) model = "gpt-4o-mini";

        log.info("[AI] 为用户 {} 创建 ChatModel: provider={}, model={}", userId, config.getLlmType(), model);
        OpenAiApi api = new OpenAiApi(baseUrl, config.getLlmApiKey());
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(model)
                .withTemperature(0.7f)
                .build();
        return new OpenAiChatModel(api, options);
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }

    private String mock(String message) {
        String resp;
        if (message.contains("你好") || message.contains("hello")) {
            resp = "你好！我是面试教练 AI，很高兴为你服务！请问你想练习哪方面的面试问题呢？\n\n💡 提示：你可以问我关于技术、HR、压力面试等任何内容！";
        } else if (message.contains("问题") || message.contains("面试")) {
            resp = "好的！以下是一个常见的面试问题：\n\n**Q: 请介绍一下你自己？**\n\n回答时可以包括：你的专业背景、工作经验、为什么对这个职位感兴趣、你的核心优势。\n\n想让我问你一个具体问题吗？";
        } else if (message.contains("技术") || message.contains("Java")) {
            resp = "技术面试通常包括：数据结构与算法、系统设计、编程语言基础、数据库知识。\n\n想让我模拟一个具体的技术问题吗？";
        } else if (message.contains("HR") || message.contains("人事")) {
            resp = "HR 面试关注：职业规划、公司匹配度、优缺点、薪资期望。需要我提供示例回答吗？";
        } else if (message.contains("压力") || message.contains("stress")) {
            resp = "压力面试测试临场反应和心理素质。想让我模拟一个压力场景吗？";
        } else {
            resp = "你可以告诉我更多上下文：面试什么岗位？遇到什么具体问题？需要帮你准备哪类问题？";
        }
        log.info("[AI] Mock 回复: {}", resp.substring(0, Math.min(resp.length(), 50)));
        return resp;
    }
}
