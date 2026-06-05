package com.interviewcoach.llm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * LLM 服务。
 * 封装 Provider 的调用，提供统一的对话入口。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private final LlmProviderFactory providerFactory;

    public String startChat(LlmContext context, LlmCallback callback) {
        return providerFactory.getProvider().startChat(context, callback);
    }

    public void sendMessage(String sessionId, String message) {
        providerFactory.getProvider().sendMessage(sessionId, message);
    }

    public void endChat(String sessionId) {
        providerFactory.getProvider().endChat(sessionId);
    }

    public CompletableFuture<LlmResponse> chat(LlmContext context, String message) {
        return providerFactory.getProvider().chat(context, message);
    }

    public List<LlmProvider.ModelInfo> getModels() {
        return providerFactory.getProvider().getModels();
    }

    public String getProviderType() {
        return providerFactory.getProvider().getType();
    }

    public boolean isAvailable() {
        return providerFactory.getProvider().isAvailable();
    }
}
