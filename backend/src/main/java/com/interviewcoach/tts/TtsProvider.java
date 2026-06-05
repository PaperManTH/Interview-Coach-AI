package com.interviewcoach.tts;

import java.util.concurrent.CompletableFuture;

/**
 * TTS Provider 抽象接口。
 * 定义语音合成的核心能力：实时流合成和离线合成。
 */
public interface TtsProvider {

    /**
     * 开始实时流合成。
     * @param context 合成上下文
     * @param callback 结果回调
     * @return 合成会话 ID
     */
    String startStreaming(TtsContext context, TtsCallback callback);

    /**
     * 发送文本进行合成。
     * @param sessionId 会话 ID
     * @param text 待合成文本
     * @param isLast 是否最后一段文本
     */
    void sendText(String sessionId, String text, boolean isLast);

    /**
     * 结束流式合成。
     * @param sessionId 会话 ID
     */
    void stopStreaming(String sessionId);

    /**
     * 离线合成（完整文本）。
     * @param context 合成上下文
     * @param text 待合成文本
     * @return 音频数据字节数组
     */
    CompletableFuture<byte[]> synthesize(TtsContext context, String text);

    /**
     * 获取支持的语音列表。
     */
    java.util.List<VoiceInfo> getVoices();

    /**
     * 获取 Provider 类型。
     */
    String getType();

    /**
     * 检查 Provider 是否可用。
     */
    boolean isAvailable();

    /**
     * 语音信息。
     */
    record VoiceInfo(String id, String name, String language, String gender) {}
}
