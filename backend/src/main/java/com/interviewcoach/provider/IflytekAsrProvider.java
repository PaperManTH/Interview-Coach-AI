package com.interviewcoach.provider;

import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;

/**
 * 讯飞语音识别 Provider（WebAPI 方式）。
 * 使用旧版 HTTP POST API，MD5 签名鉴权，无需 msc.cfg / native DLL。
 */
@Slf4j
public class IflytekAsrProvider {

    private static final String ASR_URL = "http://api.xfyun.cn/v1/service/v1/iat";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String appId;
    private final String apiKey;

    /**
     * @param combinedKey 格式: "appid:apikey"，如 "6497f9b2:your_api_key"
     */
    public IflytekAsrProvider(String combinedKey) {
        String[] parts = parseCombinedKey(combinedKey);
        this.appId = parts[0];
        this.apiKey = parts[1];
    }

    /**
     * 将 PCM 音频文件识别为文本。
     */
    public String transcribe(Path audioPath) {
        if (audioPath == null || !Files.exists(audioPath)) {
            log.error("[讯飞ASR] 音频文件不存在: {}", audioPath);
            return null;
        }

        try {
            byte[] audioBytes = Files.readAllBytes(audioPath);
            String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);

            String curTime = String.valueOf(System.currentTimeMillis() / 1000);
            String param = "{\"aue\":\"raw\",\"auf\":\"audio/L16;rate=16000\",\"engine_type\":\"sms16k\",\"scene\":\"main\"}";
            String paramBase64 = Base64.getEncoder().encodeToString(param.getBytes(StandardCharsets.UTF_8));
            String checkSum = md5(apiKey + curTime + paramBase64);

            String body = "audio=" + audioBase64;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ASR_URL))
                    .header("X-Appid", appId)
                    .header("X-CurTime", curTime)
                    .header("X-Param", paramBase64)
                    .header("X-CheckSum", checkSum)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("[讯飞ASR] 响应状态: {}, body长度: {}", response.statusCode(),
                    response.body() != null ? response.body().length() : 0);

            if (response.statusCode() == 200 && response.body() != null) {
                return parseResult(response.body());
            }
            log.error("[讯飞ASR] 失败 HTTP {}: {}", response.statusCode(), response.body());
            return null;
        } catch (Exception e) {
            log.error("[讯飞ASR] 识别异常: {}", e.getMessage());
            return null;
        }
    }

    private String parseResult(String json) {
        int dataIdx = json.indexOf("\"data\"");
        if (dataIdx == -1) {
            log.warn("[讯飞ASR] 无法解析结果: {}", json);
            return null;
        }
        int colon = json.indexOf(":", dataIdx);
        if (colon == -1) return null;

        String after = json.substring(colon + 1).trim();
        if (after.startsWith("\"")) {
            int end = after.indexOf("\"", 1);
            if (end > 0) {
                return after.substring(1, end);
            }
        }

        StringBuilder sb = new StringBuilder();
        int idx = 0;
        while ((idx = after.indexOf("\"w\"", idx)) != -1) {
            int wStart = after.indexOf("\"", idx + 4);
            if (wStart != -1) {
                int wEnd = after.indexOf("\"", wStart + 1);
                if (wEnd != -1) {
                    sb.append(after.substring(wStart + 1, wEnd));
                }
            }
            idx++;
        }
        if (sb.length() > 0) return sb.toString();

        log.warn("[讯飞ASR] 无法解析完整结果: {}", json);
        return null;
    }

    private static String[] parseCombinedKey(String combinedKey) {
        if (combinedKey == null || combinedKey.isBlank()) {
            throw new IllegalArgumentException("讯飞 ASR combinedKey 不能为空，格式: appid:apikey");
        }
        int idx = combinedKey.indexOf(':');
        if (idx <= 0) {
            throw new IllegalArgumentException("讯飞 ASR Key 格式错误，应为 appid:apikey，如 6497f9b2:your_key");
        }
        return new String[]{
                combinedKey.substring(0, idx).trim(),
                combinedKey.substring(idx + 1).trim()
        };
    }

    private String md5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
