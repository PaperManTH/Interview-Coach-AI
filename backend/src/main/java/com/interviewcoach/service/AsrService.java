package com.interviewcoach.service;

import com.interviewcoach.config.AsrProperties;
import com.interviewcoach.config.AsrProperties.AsrProviderConfig;
import com.interviewcoach.userconfig.UserProviderConfig;
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
 * 优先用户个人配置 → YML 全局配置 → Mock。
 */
@Slf4j
@Service
public class AsrService {

    @Autowired
    private UserConfigService userConfigService;

    @Autowired
    private AsrProperties asrProperties;

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * 语音转文字。
     * @param userId 用户ID
     * @param audioPath 音频文件路径（WAV/MP3 等）
     * @return 识别文本
     */
    public String transcribe(String userId, Path audioPath) {
        UserProviderConfig userCfg = userConfigService.getConfig(userId);
        String provider = resolveProvider(userCfg);
        String apiKey = resolveApiKey(userCfg, provider);
        String baseUrl = resolveBaseUrl(userCfg, provider);

        if ("mock".equalsIgnoreCase(provider) || isBlank(apiKey)) {
            return mockTranscribe(audioPath);
        }

        try {
            return callOpenAiWhisper(baseUrl, apiKey, audioPath);
        } catch (Exception e) {
            log.error("[ASR] 调用失败: {}", e.getMessage());
            return mockTranscribe(audioPath);
        }
    }

    /**
     * 语音转文字（使用 YML 全局配置，不传 userId）。
     */
    public String transcribe(Path audioPath) {
        return transcribe(null, audioPath);
    }

    // ========== 内部方法 ==========

    private String resolveProvider(UserProviderConfig userCfg) {
        if (userCfg != null && !isBlank(userCfg.getAsrType())) {
            return userCfg.getAsrType();
        }
        return asrProperties.getProvider();
    }

    private String resolveApiKey(UserProviderConfig userCfg, String provider) {
        // 优先用户配置
        if (userCfg != null && !isBlank(userCfg.getAsrApiKey())) {
            return userCfg.getAsrApiKey();
        }
        // 其次 YML 配置
        AsrProviderConfig ymlCfg = switch (provider) {
            case "openai" -> asrProperties.getOpenai();
            case "azure" -> asrProperties.getAzure();
            default -> null;
        };
        return ymlCfg != null ? ymlCfg.getApiKey() : null;
    }

    private String resolveBaseUrl(UserProviderConfig userCfg, String provider) {
        if (userCfg != null && !isBlank(userCfg.getAsrBaseUrl())) {
            return userCfg.getAsrBaseUrl();
        }
        AsrProviderConfig ymlCfg = switch (provider) {
            case "openai" -> asrProperties.getOpenai();
            case "azure" -> asrProperties.getAzure();
            default -> null;
        };
        return ymlCfg != null ? ymlCfg.getBaseUrl() : null;
    }

    /**
     * 调用 OpenAI Whisper API。
     * POST /v1/audio/transcriptions
     */
    private String callOpenAiWhisper(String baseUrl, String apiKey, Path audioPath) throws IOException, InterruptedException {
        String url = baseUrl.replaceAll("/$", "") + "/v1/audio/transcriptions";
        log.info("[ASR] 调用 Whisper API: {}", url);

        // multipart/form-data
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
        log.info("[ASR] 响应状态: {}, 内容长度: {}", response.statusCode(), response.body().length());

        if (response.statusCode() == 200) {
            return parseWhisperResponse(response.body());
        }
        log.error("[ASR] API 错误: {} - {}", response.statusCode(), response.body());
        return "语音识别失败，请重试";
    }

    private byte[] buildMultipartBody(String boundary, Path audioPath) throws IOException {
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

        byte[] headerBytes = header.getBytes();
        byte[] modelBytes = modelPart.getBytes();
        byte[] langBytes = langPart.getBytes();
        byte[] footerBytes = footer.getBytes();

        int total = headerBytes.length + fileBytes.length + modelBytes.length + langBytes.length + footerBytes.length;
        byte[] result = new byte[total];
        int pos = 0;
        System.arraycopy(headerBytes, 0, result, pos, headerBytes.length); pos += headerBytes.length;
        System.arraycopy(fileBytes, 0, result, pos, fileBytes.length); pos += fileBytes.length;
        System.arraycopy(modelBytes, 0, result, pos, modelBytes.length); pos += modelBytes.length;
        System.arraycopy(langBytes, 0, result, pos, langBytes.length); pos += langBytes.length;
        System.arraycopy(footerBytes, 0, result, pos, footerBytes.length);
        return result;
    }

    /**
     * 解析 Whisper JSON 响应：{"text": "..."}
     */
    private String parseWhisperResponse(String json) {
        // 简单解析，避免引入额外依赖
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