package com.interviewcoach.tts;

/**
 * TTS 回调接口。
 * 用于接收实时合成的音频数据。
 */
public interface TtsCallback {

    /**
     * 收到音频数据块。
     * @param audioData 音频数据
     * @param isLast 是否最后一包数据
     */
    void onAudioData(byte[] audioData, boolean isLast);

    /**
     * 合成出错。
     * @param errorMessage 错误信息
     */
    void onError(String errorMessage);

    /**
     * 合成完成。
     */
    void onComplete();
}
