package com.interviewcoach.tts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TTS Provider 测试类。
 */
class TtsProviderTest {

    private MockTtsProvider provider;

    @BeforeEach
    void setUp() {
        provider = new MockTtsProvider();
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
    void testVoices() {
        List<TtsProvider.VoiceInfo> voices = provider.getVoices();
        assertNotNull(voices);
        assertFalse(voices.isEmpty());
        System.out.println("可用语音:");
        voices.forEach(v -> System.out.println("  - " + v.id() + ": " + v.name()));
    }

    @Test
    void testSynthesize() throws Exception {
        TtsContext context = TtsContext.builder().voice("female").build();
        CompletableFuture<byte[]> future = provider.synthesize(context, "你好，世界");
        
        byte[] audio = future.get();
        assertNotNull(audio);
        assertTrue(audio.length > 0);
        System.out.println("合成音频长度: " + audio.length + " bytes");
    }

    @Test
    void testStreaming() {
        StringBuilder log = new StringBuilder();
        
        TtsCallback callback = new TtsCallback() {
            @Override
            public void onAudioData(byte[] audioData, boolean isLast) {
                log.append("audio: ").append(audioData.length).append(" bytes, last=").append(isLast).append("\n");
            }

            @Override
            public void onError(String errorMessage) {
                log.append("error: ").append(errorMessage).append("\n");
            }

            @Override
            public void onComplete() {
                log.append("complete\n");
            }
        };

        TtsContext context = TtsContext.builder().voice("female").build();
        String sessionId = provider.startStreaming(context, callback);
        
        assertNotNull(sessionId);
        
        provider.sendText(sessionId, "你好", false);
        provider.sendText(sessionId, "世界", true);
        
        System.out.println("流式合成日志:\n" + log);
        assertTrue(log.toString().contains("complete"));
    }
}
