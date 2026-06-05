package com.interviewcoach.llm;

/**
 * LLM 回调接口。
 * 用于接收流式对话的中间结果。
 */
public interface LlmCallback {

    /**
     * 收到部分响应（中间结果）。
     * @param response 响应结果
     */
    void onPartialResponse(LlmResponse response);

    /**
     * 收到最终响应。
     * @param response 响应结果
     */
    void onFinalResponse(LlmResponse response);

    /**
     * 对话出错。
     * @param errorMessage 错误信息
     */
    void onError(String errorMessage);

    /**
     * 对话完成。
     */
    void onComplete();
}
