package com.interviewcoach.service;

import java.io.IOException;
import java.nio.file.Path;

/**
 * TTS 语音合成服务接口。
 */
public interface TtsService {

    /**
     * 文字转语音，返回 Base64 编码的音频。
     */
    String synthesize(String userId, String text);
}
