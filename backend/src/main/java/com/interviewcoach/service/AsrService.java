package com.interviewcoach.service;

import java.io.IOException;
import java.nio.file.Path;

/**
 * ASR 语音识别服务接口。
 */
public interface AsrService {

    /**
     * 语音转文字。
     */
    String transcribe(String userId, Path audioPath);
}
