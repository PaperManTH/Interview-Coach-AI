package com.interviewcoach.asr;

/**
 * ASR 回调接口。
 * 用于接收实时识别结果。
 */
public interface AsrCallback {

    /**
     * 收到部分识别结果（中间结果）。
     * @param result 识别结果
     */
    void onPartialResult(AsrResult result);

    /**
     * 收到最终识别结果。
     * @param result 识别结果
     */
    void onFinalResult(AsrResult result);

    /**
     * 识别出错。
     * @param errorMessage 错误信息
     */
    void onError(String errorMessage);

    /**
     * 识别完成。
     */
    void onComplete();
}
