package com.interviewcoach.asr;

import java.util.concurrent.CompletableFuture;

/**
 * ASR Provider 抽象接口。
 * 定义语音识别的核心能力：实时流识别和离线识别。
 */
public interface AsrProvider {

    /**
     * 开始实时流识别。
     * @param context 识别上下文
     * @param callback 结果回调
     * @return 识别会话 ID
     */
    String startStreaming(AsrContext context, AsrCallback callback);

    /**
     * 发送音频数据（流式）。
     * @param sessionId 会话 ID
     * @param audioData 音频数据（PCM 格式）
     * @param isLast 是否最后一包数据
     */
    void sendAudio(String sessionId, byte[] audioData, boolean isLast);

    /**
     * 结束流式识别。
     * @param sessionId 会话 ID
     */
    void stopStreaming(String sessionId);

    /**
     * 离线识别（完整音频文件）。
     * @param context 识别上下文
     * @param audioData 完整音频数据
     * @return 识别结果
     */
    CompletableFuture<AsrResult> recognize(AsrContext context, byte[] audioData);

    /**
     * 获取 Provider 类型。
     */
    String getType();

    /**
     * 检查 Provider 是否可用。
     */
    boolean isAvailable();
}
