package com.interviewcoach.service;

import com.interviewcoach.entity.dto.ai.BilingualResponse;

/**
 * AI 对话服务接口 - 通过 LLM 生成面试回复。
 */
public interface SpringAiService {

    String chat(String message);

    String chat(String userId, String message);

    /**
     * 动态模式：带阶段信息的聊天。
     */
    String chatDynamic(String userId, String message, String stage, String focus);

    /**
     * 动态模式：带阶段信息的聊天，返回双语响应。
     */
    BilingualResponse chatDynamicBilingual(String userId, String message, String stage, String focus);

    /**
     * 动态模式：带阶段信息和会话历史上下文的聊天，返回双语响应。
     */
    BilingualResponse chatDynamicBilingual(String userId, String message, String stage, String focus, String sessionId);

    /**
     * 普通聊天，返回双语响应。
     */
    BilingualResponse chatBilingual(String userId, String message);

    /**
     * 普通聊天，携带会话历史上下文，返回双语响应。
     */
    BilingualResponse chatBilingual(String userId, String message, String sessionId);

    /**
     * 带自定义 system prompt 的对话。
     */
    String chatWithSystem(String systemMessage, String userMessage);

    /**
     * 使用模板聊天。
     */
    String chatWithTemplate(String template, java.util.Map<String, Object> params);
}
