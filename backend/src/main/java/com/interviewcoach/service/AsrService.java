package com.interviewcoach.service;

import com.interviewcoach.entity.UserProviderConfig;
import com.interviewcoach.provider.IflytekAsrProvider;

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
import java.util.UUID;

/**
 * ASR 语音识别服务。
 * 
 * 配置来源：用户个人配置（UserProviderConfig）
 * 默认模式：Mock（无用户配置时）
 */
@Slf4j
@Service
public class AsrService {

    @Autowired
    private UserConfigService userConfigService;

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * 语音转文字。
     */
    public String transcribe(String userId, Path audioPath) {
        UserProviderConfig userCfg = userConfigService.getConfig(userId);
        
        // 未配置时使用 Mock
        if (userCfg == null || isBlank(userCfg.getAsrType()) || "mock".equalsIgnoreCase(userCfg.getAsrType())) {
            log.info("[ASR] Mock 模式（无用户配置）");
            return mockTranscribe(audioPath);
        }

        if ("iflytek".equalsIgnoreCase(userCfg.getAsrType())) {
            return transcribeWithIflytek(userCfg, audioPath);
        }

        // OpenAI Whisper API
        return transcribeWithWhisper(userCfg, audioPath);
    }

    public String transcribe(Path audioPath) {
        return transcribe(null, audioPath);
    }

    // ========== 讯飞 ASR ==========

    private String transcribeWithIflytek(UserProviderConfig userCfg, Path audioPath) {
        String apiKey = userCfg.getAsrApiKey();
        if (isBlank(apiKey)) {
            log.warn("[ASR] 讯飞 API Key 未配置");
            return mockTranscribe(audioPath);
        }
        
        try {
            IflytekAsrProvider provider = new IflytekAsrProvider(apiKey);
            String result = provider.transcribe(audioPath);
            return result != null && !result.isEmpty() ? result : "语音识别暂无结果";
        } catch (Exception e) {
            log.error("[ASR] 讯飞识别失败: {}", e.getMessage());
            return mockTranscribe(audioPath);
        }
    }

    // ========== Whisper API ==========

    private String transcribeWithWhisper(UserProviderConfig userCfg, Path audioPath) {
        String apiKey = userCfg.getAsrApiKey();
        String baseUrl = userCfg.getAsrBaseUrl();
        
        if (isBlank(apiKey) || isBlank(baseUrl)) {
            log.warn("[ASR] Whisper API 配置不完整");
            return mockTranscribe(audioPath);
        }

        try {
            return callWhisperApi(baseUrl, apiKey, audioPath);
        } catch (Exception e) {
            log.error("[ASR] Whisper 调用失败: {}", e.getMessage());
            return mockTranscribe(audioPath);
        }
    }

    private String callWhisperApi(String baseUrl, String apiKey, Path audioPath) throws IOException, InterruptedException {
        String url = baseUrl.replaceAll("/$", "") + "/v1/audio/transcriptions";
        log.info("[ASR] 调用 Whisper API: {}", url);

        String boundary = "----" + UUID.randomUUID();
        byte[] body = buildMultipartBody(boundary, audioPath);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseWhisperResponse(response.body());
        }
        log.error("[ASR] API 错误: {} - {}", response.statusCode(), response.body());
        return "语音识别失败，请重试";
    }

    private byte[] buildMultipartBody(String boundary, Path audioPath) throws IOException {
        if (audioPath == null) {
            return new byte[0];
        }
        
        String filename = audioPath.getFileName().toString();
        String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf(".") + 1) : "wav";
        String mime = switch (ext) {
            case "mp3" -> "audio/mpeg";
            case "m4a" -> "audio/mp4";
            case "ogg" -> "audio/ogg";
            default -> "audio/wav";
        };

        byte[] fileBytes = Files.readAllBytes(audioPath);
        String header = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n"
                + "Content-Type: " + mime + "\r\n\r\n";
        String footer = "\r\n--" + boundary + "--\r\n";
        String modelPart = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"model\"\r\n\r\nwhisper-1\r\n";
        String langPart = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"language\"\r\n\r\nzh\r\n";

        return concatenate(header.getBytes(), fileBytes, modelPart.getBytes(), langPart.getBytes(), footer.getBytes());
    }

    private byte[] concatenate(byte[]... parts) {
        int total = 0;
        for (byte[] part : parts) total += part.length;
        byte[] result = new byte[total];
        int pos = 0;
        for (byte[] part : parts) {
            System.arraycopy(part, 0, result, pos, part.length);
            pos += part.length;
        }
        return result;
    }

    private String parseWhisperResponse(String json) {
        int start = json.indexOf("\"text\"");
        if (start == -1) return json;
        start = json.indexOf("\"", start + 6);
        if (start == -1) return json;
        int end = json.indexOf("\"", start + 1);
        if (end == -1) return json;
        return json.substring(start + 1, end)
                .replace("\\n", "\n")
                .replace("\\\"", "\"");
    }

    private String mockTranscribe(Path audioPath) {
        log.info("[ASR] Mock 模式 - 模拟语音识别");
        return "这是语音识别结果的模拟回复";
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}
