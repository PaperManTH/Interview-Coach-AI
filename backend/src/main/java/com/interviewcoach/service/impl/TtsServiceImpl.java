package com.interviewcoach.service.impl;

import com.interviewcoach.service.TtsService;
import com.interviewcoach.common.Constants;

import com.interviewcoach.entity.UserProviderConfig;
import com.interviewcoach.provider.IflytekTtsProvider;

import com.interviewcoach.service.UserConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;

/**
 * TTS 语音合成服务。
 * 
 * 配置来源：用户个人配置（UserProviderConfig）
 * 默认模式：Mock（无用户配置时）
 */
@Slf4j
@Service
public class TtsServiceImpl implements TtsService {

    @Autowired
    private UserConfigService userConfigService;

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * 文字转语音，返回 Base64 编码的音频。
     */
    public String synthesize(String userId, String text) {
        UserProviderConfig userCfg = userConfigService.getConfig(userId);
        
        // 未配置时使用 Mock
        if (userCfg == null || isBlank(userCfg.getTtsType()) || Constants.PROVIDER_MOCK.equalsIgnoreCase(userCfg.getTtsType())) {
            log.info("[TTS] Mock 模式（无用户配置）");
            return null;
        }

        if (Constants.PROVIDER_IFLYTEK.equalsIgnoreCase(userCfg.getTtsType())) {
            return synthesizeWithIflytek(userCfg, text);
        }

        // OpenAI / Azure 等通用 API
        return synthesizeWithOpenAiApi(userCfg, text);
    }

    // ========== 讯飞超拟人 TTS ==========

    private String synthesizeWithIflytek(UserProviderConfig userCfg, String text) {
        String apiKey = userCfg.getTtsApiKey();
        if (isBlank(apiKey)) {
            log.warn("[TTS] 讯飞 API Key 未配置");
            return null;
        }
        
        String voice = userCfg.getTtsVoice();
        if (isBlank(voice)) {
            voice = "x5_lingfeiyi_flow";
        }
        
        try {
            IflytekTtsProvider provider = new IflytekTtsProvider(apiKey, voice);
            return provider.synthesize(text);
        } catch (Exception e) {
            log.error("[TTS] 讯飞合成失败: {}", e.getMessage());
            return null;
        }
    }

    // ========== 通用 OpenAI API ==========

    private String synthesizeWithOpenAiApi(UserProviderConfig userCfg, String text) {
        String apiKey = userCfg.getTtsApiKey();
        String baseUrl = userCfg.getTtsBaseUrl();
        String model = "tts-1";
        String voice = userCfg.getTtsVoice();
        
        if (isBlank(apiKey) || isBlank(baseUrl)) {
            log.warn("[TTS] API 配置不完整");
            return null;
        }
        
        if (isBlank(voice)) {
            voice = "alloy";
        }

        try {
            byte[] audio = callTtsApi(baseUrl, apiKey, model, voice, text);
            log.info("[TTS] 合成成功, 音频大小: {} bytes", audio.length);
            return Base64.getEncoder().encodeToString(audio);
        } catch (Exception e) {
            log.error("[TTS] API 调用失败: {}", e.getMessage());
            return null;
        }
    }

    private byte[] callTtsApi(String baseUrl, String apiKey, String model, String voice, String text)
            throws IOException, InterruptedException {
        String url = baseUrl.replaceAll("/$", "") + "/v1/audio/speech";
        log.info("[TTS] 调用 API: {}", url);

        String json = String.format(
                "{\"model\":\"%s\",\"input\":\"%s\",\"voice\":\"%s\",\"response_format\":\"mp3\"}",
                model, escapeJson(text), voice
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        
        if (response.statusCode() == 200) {
            return response.body();
        }
        throw new IOException("TTS API 返回 " + response.statusCode());
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}
