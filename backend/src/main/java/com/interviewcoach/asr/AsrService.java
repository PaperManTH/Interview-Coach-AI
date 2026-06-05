package com.interviewcoach.asr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * ASR 服务。
 * 封装 Provider 的调用，提供统一的语音识别入口。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsrService {

    private final AsrProviderFactory providerFactory;

    /**
     * 开始实时流识别。
     * @param context 识别上下文
     * @param callback 结果回调
     * @return 会话 ID
     */
    public String startStreaming(AsrContext context, AsrCallback callback) {
        return providerFactory.getProvider().startStreaming(context, callback);
    }

    /**
     * 发送音频数据。
     */
    public void sendAudio(String sessionId, byte[] audioData, boolean isLast) {
        providerFactory.getProvider().sendAudio(sessionId, audioData, isLast);
    }

    /**
     * 停止流式识别。
     */
    public void stopStreaming(String sessionId) {
        providerFactory.getProvider().stopStreaming(sessionId);
    }

    /**
     * 离线识别。
     */
    public CompletableFuture<AsrResult> recognize(AsrContext context, byte[] audioData) {
        return providerFactory.getProvider().recognize(context, audioData);
    }

    /**
     * 获取当前 Provider 类型。
     */
    public String getProviderType() {
        return providerFactory.getProvider().getType();
    }

    /**
     * 检查服务是否可用。
     */
    public boolean isAvailable() {
        return providerFactory.getProvider().isAvailable();
    }
}
