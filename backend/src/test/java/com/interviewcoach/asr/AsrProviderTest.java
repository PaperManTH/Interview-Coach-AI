package com.interviewcoach.asr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ASR Provider 测试类。
 */
class AsrProviderTest {

    private MockAsrProvider provider;

    @BeforeEach
    void setUp() {
        provider = new MockAsrProvider();
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
    void testOfflineRecognize() throws Exception {
        AsrContext context = AsrContext.builder().language("zh_CN").build();
        CompletableFuture<AsrResult> future = provider.recognize(context, new byte[]{1, 2, 3});
        
        AsrResult result = future.get();
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getText());
        System.out.println("离线识别结果: " + result.getText());
    }

    @Test
    void testStreaming() {
        StringBuilder results = new StringBuilder();
        
        AsrCallback callback = new AsrCallback() {
            @Override
            public void onPartialResult(AsrResult result) {
                results.append("partial: ").append(result.getText()).append("\n");
            }

            @Override
            public void onFinalResult(AsrResult result) {
                results.append("final: ").append(result.getText()).append("\n");
            }

            @Override
            public void onError(String errorMessage) {
                results.append("error: ").append(errorMessage).append("\n");
            }

            @Override
            public void onComplete() {
                results.append("complete\n");
            }
        };

        AsrContext context = AsrContext.builder().language("zh_CN").build();
        String sessionId = provider.startStreaming(context, callback);
        
        assertNotNull(sessionId);
        assertFalse(sessionId.isEmpty());
        
        provider.sendAudio(sessionId, new byte[]{1, 2, 3}, false);
        provider.sendAudio(sessionId, new byte[]{4, 5, 6}, true);
        
        System.out.println("流式识别结果:\n" + results);
        assertTrue(results.toString().contains("complete"));
    }
}
