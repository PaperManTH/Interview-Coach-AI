package com.interviewcoach.llm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LLM Provider 测试类。
 */
class LlmProviderTest {

    private MockLlmProvider provider;

    @BeforeEach
    void setUp() {
        provider = new MockLlmProvider();
    }

    @Test
    void testType() {
        assertEquals("MOCK", provider.getType());
    }

    @Test
    void testAvailability() {
        assertTrue(provider.isAvailable());
    }

    @Test
    void testModels() {
        List<LlmProvider.ModelInfo> models = provider.getModels();
        assertNotNull(models);
        assertFalse(models.isEmpty());
        System.out.println("可用模型:");
        models.forEach(m -> System.out.println("  - " + m.id() + ": " + m.name()));
    }

    @Test
    void testChat() throws Exception {
        LlmContext context = LlmContext.builder()
                .model("gpt-4o-mini")
                .temperature(0.7)
                .build();
        
        CompletableFuture<LlmResponse> future = provider.chat(context, "你好");
        
        LlmResponse response = future.get();
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getText());
        System.out.println("LLM 响应: " + response.getText());
    }

    @Test
    void testStreamingChat() {
        StringBuilder responses = new StringBuilder();
        
        LlmCallback callback = new LlmCallback() {
            @Override
            public void onPartialResponse(LlmResponse response) {
                responses.append("partial: ").append(response.getText()).append("\n");
            }

            @Override
            public void onFinalResponse(LlmResponse response) {
                responses.append("final: ").append(response.getText()).append("\n");
            }

            @Override
            public void onError(String errorMessage) {
                responses.append("error: ").append(errorMessage).append("\n");
            }

            @Override
            public void onComplete() {
                responses.append("complete\n");
            }
        };

        LlmContext context = LlmContext.builder().model("gpt-4o-mini").build();
        String sessionId = provider.startChat(context, callback);
        
        assertNotNull(sessionId);
        provider.sendMessage(sessionId, "请分析这个问题");
        
        System.out.println("流式对话结果:\n" + responses);
        assertTrue(responses.toString().contains("complete"));
    }
}
