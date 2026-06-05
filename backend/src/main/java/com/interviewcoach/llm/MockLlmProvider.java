package com.interviewcoach.llm;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock LLM Provider。
 * 用于开发和测试环境，返回预设的对话响应。
 */
@Slf4j
public class MockLlmProvider implements LlmProvider {

    private final Map<String, LlmCallback> sessions = new ConcurrentHashMap<>();

    private static final List<ModelInfo> MODELS = List.of(
        new ModelInfo("gpt-4o-mini", "GPT-4o Mini", "轻量级模型，响应速度快"),
        new ModelInfo("gpt-4o", "GPT-4o", "高质量模型")
    );

    @Override
    public String startChat(LlmContext context, LlmCallback callback) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, callback);
        log.info("[LLM-Mock] 开始对话 sessionId={}", sessionId);
        return sessionId;
    }

    @Override
    public void sendMessage(String sessionId, String message) {
        LlmCallback callback = sessions.get(sessionId);
        if (callback == null) return;

        String mockResponse = generateMockResponse(message);
        callback.onPartialResponse(LlmResponse.partial(mockResponse));
        callback.onFinalResponse(LlmResponse.success(mockResponse));
        callback.onComplete();
        sessions.remove(sessionId);
    }

    @Override
    public void endChat(String sessionId) {
        sessions.remove(sessionId);
        log.info("[LLM-Mock] 结束对话 sessionId={}", sessionId);
    }

    @Override
    public CompletableFuture<LlmResponse> chat(LlmContext context, String message) {
        return CompletableFuture.completedFuture(LlmResponse.success(generateMockResponse(message)));
    }

    @Override
    public List<ModelInfo> getModels() {
        return MODELS;
    }

    @Override
    public String getType() {
        return "MOCK";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private String generateMockResponse(String message) {
        String[] mockResponses = {
            "这是一个很好的问题！让我来分析一下。",
            "根据你的问题，我认为关键点在于以下几个方面：",
            "感谢你的提问，我将从几个角度来解答。",
            "这个问题涉及到多个技术层面，我来逐一分析。",
            "好的，我来帮你分析这个问题。"
        };
        return mockResponses[(int) (Math.random() * mockResponses.length)];
    }
}
