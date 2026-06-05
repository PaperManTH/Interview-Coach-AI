package com.interviewcoach.llm;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * LLM Provider 抽象接口。
 * 定义大语言模型的核心能力：流式响应和完整响应。
 */
public interface LlmProvider {

    /**
     * 开始流式对话。
     * @param context 对话上下文
     * @param callback 结果回调
     * @return 对话会话 ID
     */
    String startChat(LlmContext context, LlmCallback callback);

    /**
     * 发送消息（流式）。
     * @param sessionId 会话 ID
     * @param message 用户消息
     */
    void sendMessage(String sessionId, String message);

    /**
     * 结束对话。
     * @param sessionId 会话 ID
     */
    void endChat(String sessionId);

    /**
     * 非流式对话（完整响应）。
     * @param context 对话上下文
     * @param message 用户消息
     * @return 对话响应
     */
    CompletableFuture<LlmResponse> chat(LlmContext context, String message);

    /**
     * 获取支持的模型列表。
     */
    List<ModelInfo> getModels();

    /**
     * 获取 Provider 类型。
     */
    String getType();

    /**
     * 检查 Provider 是否可用。
     */
    boolean isAvailable();

    /**
     * 模型信息。
     */
    record ModelInfo(String id, String name, String description) {}
}
