package com.interviewcoach.asr;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock ASR Provider。
 * 用于开发和测试环境，返回预设的识别结果。
 */
@Slf4j
public class MockAsrProvider implements AsrProvider {

    private final Map<String, AsrCallback> sessions = new ConcurrentHashMap<>();

    @Override
    public String startStreaming(AsrContext context, AsrCallback callback) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, callback);
        log.info("[ASR-Mock] 开始流式识别 sessionId={}", sessionId);
        return sessionId;
    }

    @Override
    public void sendAudio(String sessionId, byte[] audioData, boolean isLast) {
        AsrCallback callback = sessions.get(sessionId);
        if (callback == null) return;

        if (!isLast) {
            callback.onPartialResult(AsrResult.partial("正在识别中..."));
        } else {
            String mockResult = generateMockResult();
            callback.onFinalResult(AsrResult.success(mockResult));
            callback.onComplete();
            sessions.remove(sessionId);
        }
    }

    @Override
    public void stopStreaming(String sessionId) {
        sessions.remove(sessionId);
        log.info("[ASR-Mock] 停止流式识别 sessionId={}", sessionId);
    }

    @Override
    public CompletableFuture<AsrResult> recognize(AsrContext context, byte[] audioData) {
        return CompletableFuture.completedFuture(AsrResult.success(generateMockResult()));
    }

    @Override
    public String getType() {
        return "MOCK";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private String generateMockResult() {
        String[] mockResults = {
            "你好，我是面试助手。",
            "请开始你的自我介绍。",
            "这个问题问得很好。",
            "我来分析一下这个技术点。",
            "感谢你的回答。"
        };
        return mockResults[(int) (Math.random() * mockResults.length)];
    }
}
