package com.interviewcoach.service;

import com.interviewcoach.config.ChatModelConfig;
import com.interviewcoach.entity.UserProviderConfig;
import com.interviewcoach.mapper.ResumeProfileMapper;
import com.interviewcoach.entity.ResumeProfile;
import com.interviewcoach.entity.dto.ai.BilingualResponse;
import com.interviewcoach.tool.ResumeTool;
import com.interviewcoach.utils.BilingualResponseParser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackContext;
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
 * 支持动态面试模式的阶段感知 Prompt 策略。
 */
@Slf4j
@Service
public class SpringAiService {

    @Autowired(required = false)
    private ChatModel chatModel;

    @Autowired
    private UserConfigService userConfigService;

    @Autowired(required = false)
    private ResumeProfileMapper resumeProfileMapper;

    @Autowired
    private ChatModelConfig chatModelConfig;

    /** 注册的 FunctionCallback（ResumeTool 等），LLM 可在对话中调用 */
    @Autowired(required = false)
    private List<FunctionCallback> functionCallbacks;

    @Autowired(required = false)
    private FunctionCallbackContext functionCallbackContext;

    /** 缓存用户 ChatModel，避免每次请求都创建 */
    private final ConcurrentHashMap<String, ChatModel> userModelCache = new ConcurrentHashMap<>();

    public String chat(String message) {
        return internalChat(message, chatModelConfig.getSystemPrompt());
    }

    public String chat(String userId, String message) {
        return internalChatWithUser(userId, message, chatModelConfig.getSystemPrompt());
    }

    /**
     * 动态模式：带阶段信息的聊天
     */
    public String chatDynamic(String userId, String message, String stage, String focus) {
        String systemPrompt = buildDynamicSystemPrompt(userId, stage, focus);
        log.info("[AI-Dynamic] userId={}, stage={}, focus={}", userId, stage, focus);
        return internalChatWithUser(userId, message, systemPrompt);
    }

    /**
     * 动态模式：带阶段信息的聊天，返回双语响应
     */
    public BilingualResponse chatDynamicBilingual(String userId, String message, String stage, String focus) {
        String rawResponse = chatDynamic(userId, message, stage, focus);
        BilingualResponse response = BilingualResponseParser.parse(rawResponse);

        if (response == null || !response.isValid()) {
            log.warn("[AI-Bilingual] 响应非标准 JSON，包装为双语文本");
            String safeText = (rawResponse == null || rawResponse.isBlank()) ? "Let's continue the interview." : rawResponse;
            String english = safeText.substring(0, Math.min(500, safeText.length()));
            return new BilingualResponse(safeText, english);
        }

        log.info("[AI-Bilingual] 解析成功，中文长度={}，英文长度={}",
                response.getChinese().length(), response.getEnglish().length());
        return response;
    }

    /**
     * 普通聊天，返回双语响应
     */
    public BilingualResponse chatBilingual(String userId, String message) {
        String rawResponse = chat(userId, message);
        BilingualResponse response = BilingualResponseParser.parse(rawResponse);

        if (response == null || !response.isValid()) {
            log.warn("[AI-Bilingual] 响应非标准 JSON，包装为双语文本");
            String safeText = (rawResponse == null || rawResponse.isBlank()) ? "Let's continue the interview." : rawResponse;
            String english = safeText.substring(0, Math.min(500, safeText.length()));
            return new BilingualResponse(safeText, english);
        }

        return response;
    }

    private String buildDynamicSystemPrompt(String userId, String stage, String focus) {
        String basePrompt = chatModelConfig.getPromptByStage(stage);
        
        if ("technical".equals(stage)) {
            basePrompt = buildTechnicalPrompt(basePrompt, userId, focus);
        }

        // 告知 LLM 可调用工具
        if (functionCallbacks != null && !functionCallbacks.isEmpty()) {
            basePrompt += "\n\n你可以调用 updateUserResume 工具来保存用户在对话中透露的技能、项目经验、工作经历等信息到简历数据库。";
        }

        return basePrompt + "\n\n当前阶段：" + stage + " | 面试重点：" + focus;
    }

    private String buildTechnicalPrompt(String basePrompt, String userId, String focus) {
        StringBuilder prompt = new StringBuilder(basePrompt);

        if ("resume".equals(focus) && resumeProfileMapper != null && userId != null) {
            try {
                List<ResumeProfile> profiles = resumeProfileMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ResumeProfile>()
                                .eq(ResumeProfile::getUserId, userId)
                                .orderByDesc(ResumeProfile::getId)
                                .last("LIMIT 1")
                );
                if (!profiles.isEmpty() && profiles.get(0).getParsedJson() != null) {
                    String resumeContext = profiles.get(0).getParsedJson();
                    if (resumeContext.length() > 2000) {
                        resumeContext = resumeContext.substring(0, 2000) + "...";
                    }
                    prompt.append("\n\n【候选人简历信息】\n").append(resumeContext);
                    prompt.append("\n\n请基于以上简历信息，针对性地提出技术问题。");
                    log.info("[AI-Dynamic] 已加载用户 {} 的简历上下文", userId);
                }
            } catch (Exception e) {
                log.warn("[AI-Dynamic] 加载简历失败: {}", e.getMessage());
            }
        } else if ("technical".equals(focus)) {
            prompt.append("\n\n聚焦于纯技术深度，覆盖：算法与数据结构、系统设计、编程语言原理、数据库优化、分布式系统。");
        } else {
            prompt.append("\n\n通用技术面试，平衡覆盖：项目经验、技术栈、系统设计、问题解决能力。");
        }

        return prompt.toString();
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
            ResumeTool.setCurrentUserId(userId);

            List<Message> messages = List.of(
                    new SystemMessage(systemMessage),
                    new UserMessage(userMessage)
            );

            Prompt prompt;
            if (functionCallbacks != null && !functionCallbacks.isEmpty()) {
                OpenAiChatOptions.Builder optBuilder = OpenAiChatOptions.builder();
                for (FunctionCallback cb : functionCallbacks) {
                    optBuilder.withFunction(cb.getName());
                }
                prompt = new Prompt(messages, optBuilder.build());
            } else {
                prompt = new Prompt(messages);
            }

            String content = model.call(prompt).getResult().getOutput().getContent();
            log.info("[AI] 响应长度: {} chars", content != null ? content.length() : 0);
            return content != null ? content : "";
        } catch (Exception e) {
            String rawMsg = e.getMessage() == null ? "unknown error" : e.getMessage();
            String lowerMsg = rawMsg.toLowerCase();

            if (rawMsg.startsWith("402") || lowerMsg.contains("insufficient balance") || lowerMsg.contains("quota")) {
                log.error("[AI] Provider 余额不足 (402), 降级到模拟回复: {}", rawMsg);
            } else if (rawMsg.startsWith("429") || lowerMsg.contains("rate limit") || lowerMsg.contains("too many")) {
                log.error("[AI] Provider 限流 (429), 降级到模拟回复: {}", rawMsg);
            } else if (rawMsg.startsWith("401") || rawMsg.startsWith("403") || lowerMsg.contains("unauthorized") || lowerMsg.contains("invalid api")) {
                log.error("[AI] Provider 鉴权失败 (401/403), 降级到模拟回复: {}", rawMsg);
            } else {
                log.error("[AI] ChatModel 调用失败: {}", rawMsg);
            }

            return mock(userMessage);
        } finally {
            ResumeTool.clearCurrentUserId();
        }
    }

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
        return chatModel;
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
        return new OpenAiChatModel(api, options, functionCallbackContext, null);
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
