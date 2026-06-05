package com.interviewcoach.service;

import com.interviewcoach.config.TtsProperties;
import com.interviewcoach.config.TtsProperties.TtsProviderConfig;
import com.interviewcoach.entity.UserProviderConfig;
import com.interviewcoach.provider.IflytekTtsProvider;

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
 * 优先用户个人配置 → YML 全局配置 → Mock。
 */
@Slf4j
@Service
public class TtsService {

    @Autowired
    private UserConfigService userConfigService;

    @Autowired
    private TtsProperties ttsProperties;

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * 文字转语音，返回 Base64 编码的音频。
     * @param userId 用户ID
     * @param text 要合成的文本
     * @return Base64 音频字符串
     */
    public String synthesize(String userId, String text) {
        UserProviderConfig userCfg = userConfigService.getConfig(userId);
        String provider = resolveProvider(userCfg);

        if ("mock".equalsIgnoreCase(provider)) {
            log.info("[TTS] Mock 模式");
            return null;
        }

        if ("iflytek".equalsIgnoreCase(provider)) {
            String userApiKey = userCfg != null ? userCfg.getTtsApiKey() : null;
            return synthesizeWithIflytek(text, userApiKey);
        }

        String apiKey = resolveApiKey(userCfg, provider);
        if (isBlank(apiKey)) {
            log.info("[TTS] Mock 模式（无 API Key）");
            return null;
        }

        String baseUrl = resolveBaseUrl(userCfg, provider);
        String voice = resolveVoice(userCfg, provider);
        String model = resolveModel(provider);

        try {
            byte[] audio = callOpenAiTts(baseUrl, apiKey, model, voice, text);
            log.info("[TTS] 合成成功, 音频大小: {} bytes", audio.length);
            return Base64.getEncoder().encodeToString(audio);
        } catch (Exception e) {
            log.error("[TTS] 调用失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 文字转语音（使用 YML 全局配置）。
     */
    public String synthesize(String text) {
        return synthesize(null, text);
    }

    /**
     * 文字转语音，存入文件。
     */
    public Path synthesizeToFile(String userId, String text, Path outputPath) throws IOException {
        UserProviderConfig userCfg = userConfigService.getConfig(userId);
        String provider = resolveProvider(userCfg);

        if ("mock".equalsIgnoreCase(provider)) {
            log.info("[TTS] Mock 模式，跳过文件生成");
            return null;
        }

        if ("iflytek".equalsIgnoreCase(provider)) {
            String userApiKey = userCfg != null ? userCfg.getTtsApiKey() : null;
            String base64 = synthesizeWithIflytek(text, userApiKey);
            if (base64 != null) {
                byte[] audio = Base64.getDecoder().decode(base64);
                Files.write(outputPath, audio);
                log.info("[TTS] 讯飞音频已写入: {}", outputPath);
                return outputPath;
            }
            return null;
        }

        String apiKey = resolveApiKey(userCfg, provider);
        if (isBlank(apiKey)) {
            log.info("[TTS] Mock 模式，跳过文件生成");
            return null;
        }

        String baseUrl = resolveBaseUrl(userCfg, provider);
        String voice = resolveVoice(userCfg, provider);
        String model = resolveModel(provider);

        try {
            byte[] audio = callOpenAiTts(baseUrl, apiKey, model, voice, text);
            Files.write(outputPath, audio);
            log.info("[TTS] 音频已写入: {}", outputPath);
            return outputPath;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("TTS 调用被中断", e);
        }
    }

    // ========== 讯飞 TTS ==========

    private String synthesizeWithIflytek(String text, String userApiKey) {
        log.info("[TTS] 使用讯飞语音合成, textLen={}", text.length());
        try {
            IflytekTtsProvider provider = new IflytekTtsProvider(userApiKey, "xiaoyan");
            return provider.synthesize(text);
        } catch (Exception e) {
            log.error("[TTS] 讯飞合成失败: {}", e.getMessage());
            return null;
        }
    }

    // ========== 内部方法 ==========

    private String resolveProvider(UserProviderConfig userCfg) {
        if (userCfg != null && !isBlank(userCfg.getTtsType())) {
            return userCfg.getTtsType();
        }
        return ttsProperties.getProvider();
    }

    private String resolveApiKey(UserProviderConfig userCfg, String provider) {
        if (userCfg != null && !isBlank(userCfg.getTtsApiKey())) {
            return userCfg.getTtsApiKey();
        }
        TtsProviderConfig ymlCfg = switch (provider) {
            case "openai" -> ttsProperties.getOpenai();
            case "azure" -> ttsProperties.getAzure();
            default -> null;
        };
        return ymlCfg != null ? ymlCfg.getApiKey() : null;
    }

    private String resolveBaseUrl(UserProviderConfig userCfg, String provider) {
        if (userCfg != null && !isBlank(userCfg.getTtsBaseUrl())) {
            return userCfg.getTtsBaseUrl();
        }
        TtsProviderConfig ymlCfg = switch (provider) {
            case "openai" -> ttsProperties.getOpenai();
            case "azure" -> ttsProperties.getAzure();
            default -> null;
        };
        return ymlCfg != null ? ymlCfg.getBaseUrl() : null;
    }

    private String resolveVoice(UserProviderConfig userCfg, String provider) {
        if (userCfg != null && !isBlank(userCfg.getTtsVoice())) {
            return userCfg.getTtsVoice();
        }
        TtsProviderConfig ymlCfg = switch (provider) {
            case "openai" -> ttsProperties.getOpenai();
            case "azure" -> ttsProperties.getAzure();
            default -> null;
        };
        return ymlCfg != null ? ymlCfg.getVoice() : "alloy";
    }

    private String resolveModel(String provider) {
        return switch (provider) {
            case "openai" -> ttsProperties.getOpenai().getModel();
            case "azure" -> "tts";
            default -> "tts-1";
        };
    }

    /**
     * 调用 OpenAI TTS API。
     */
    private byte[] callOpenAiTts(String baseUrl, String apiKey, String model, String voice, String text)
            throws IOException, InterruptedException {
        String url = baseUrl.replaceAll("/$", "") + "/v1/audio/speech";
        log.info("[TTS] 调用 TTS API: {}, voice={}, textLen={}", url, voice, text.length());

        String json = String.format(
                "{\"model\":\"%s\",\"input\":\"%s\",\"voice\":\"%s\",\"response_format\":\"mp3\"}",
                model,
                escapeJson(text),
                voice
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        log.info("[TTS] 响应状态: {}, 音频大小: {} bytes", response.statusCode(),
                response.body() != null ? response.body().length : 0);

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
