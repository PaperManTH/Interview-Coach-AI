package com.interviewcoach.controller;

import com.interviewcoach.dto.common.ApiResponse;
import com.interviewcoach.service.TtsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * TTS 语音合成测试控制器。
 */
@Slf4j
@RestController
@RequestMapping("/api/tts")
@RequiredArgsConstructor
public class TtsController {

    private final TtsService ttsService;

    @PostMapping("/synthesize")
    public ResponseEntity<ApiResponse<String>> synthesize(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String userId = request.getOrDefault("userId", "current-user");
        log.info("[TTS] 合成请求: userId={}, textLen={}", userId, text != null ? text.length() : 0);

        if (text == null || text.isBlank()) {
            return ResponseEntity.ok(ApiResponse.error(400, "text 不能为空"));
        }

        String audioBase64 = ttsService.synthesize(userId, text);
        if (audioBase64 == null) {
            return ResponseEntity.ok(ApiResponse.error(500, "TTS 合成失败或 Mock 模式"));
        }

        log.info("[TTS] 合成成功, Base64长度={}", audioBase64.length());
        return ResponseEntity.ok(ApiResponse.ok(audioBase64));
    }
}
