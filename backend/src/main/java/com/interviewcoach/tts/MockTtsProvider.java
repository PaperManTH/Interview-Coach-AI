package com.interviewcoach.tts;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock TTS Provider。
 * 用于开发和测试环境，返回预设的音频数据。
 */
@Slf4j
public class MockTtsProvider implements TtsProvider {

    private final Map<String, TtsCallback> sessions = new ConcurrentHashMap<>();

    private static final List<VoiceInfo> VOICES = List.of(
        new VoiceInfo("female", "女声", "zh_CN", "female"),
        new VoiceInfo("male", "男声", "zh_CN", "male")
    );

    @Override
    public String startStreaming(TtsContext context, TtsCallback callback) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, callback);
        log.info("[TTS-Mock] 开始流式合成 sessionId={}", sessionId);
        return sessionId;
    }

    @Override
    public void sendText(String sessionId, String text, boolean isLast) {
        TtsCallback callback = sessions.get(sessionId);
        if (callback == null) return;

        byte[] mockAudio = generateMockAudio(text);
        callback.onAudioData(mockAudio, isLast);

        if (isLast) {
            callback.onComplete();
            sessions.remove(sessionId);
        }
    }

    @Override
    public void stopStreaming(String sessionId) {
        sessions.remove(sessionId);
        log.info("[TTS-Mock] 停止流式合成 sessionId={}", sessionId);
    }

    @Override
    public CompletableFuture<byte[]> synthesize(TtsContext context, String text) {
        return CompletableFuture.completedFuture(generateMockAudio(text));
    }

    @Override
    public List<VoiceInfo> getVoices() {
        return VOICES;
    }

    @Override
    public String getType() {
        return "MOCK";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private byte[] generateMockAudio(String text) {
        byte[] header = {0x52, 0x49, 0x46, 0x46, 0x24, 0x08, 0x00, 0x00, 0x57, 0x41, 0x56, 0x45};
        byte[] data = new byte[header.length + text.length() * 2];
        System.arraycopy(header, 0, data, 0, header.length);
        return data;
    }
}
