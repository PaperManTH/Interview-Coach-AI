package com.interviewcoach.tts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * TTS 服务。
 * 封装 Provider 的调用，提供统一的语音合成入口。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TtsService {

    private final TtsProviderFactory providerFactory;

    public String startStreaming(TtsContext context, TtsCallback callback) {
        return providerFactory.getProvider().startStreaming(context, callback);
    }

    public void sendText(String sessionId, String text, boolean isLast) {
        providerFactory.getProvider().sendText(sessionId, text, isLast);
    }

    public void stopStreaming(String sessionId) {
        providerFactory.getProvider().stopStreaming(sessionId);
    }

    public CompletableFuture<byte[]> synthesize(TtsContext context, String text) {
        return providerFactory.getProvider().synthesize(context, text);
    }

    public List<TtsProvider.VoiceInfo> getVoices() {
        return providerFactory.getProvider().getVoices();
    }

    public String getProviderType() {
        return providerFactory.getProvider().getType();
    }

    public boolean isAvailable() {
        return providerFactory.getProvider().isAvailable();
    }
}
