package com.interviewcoach.provider;

import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;

/**
 * 讯飞语音合成 Provider（WebAPI 方式）。
 * 使用旧版 HTTP POST API，MD5 签名鉴权，无需 msc.cfg / native DLL。
 */
@Slf4j
public class IflytekTtsProvider {

    private static final String TTS_URL = "http://api.xfyun.cn/v1/service/v1/tts";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String appId;
    private final String apiKey;
    private final String voice;

    /**
     * @param combinedKey 格式: "appid:apikey"，如 "6497f9b2:your_api_key"
     * @param voice       发音人，如 "xiaoyan"
     */
    public IflytekTtsProvider(String combinedKey, String voice) {
        String[] parts = parseCombinedKey(combinedKey);
        this.appId = parts[0];
        this.apiKey = parts[1];
        this.voice = voice != null ? voice : "xiaoyan";
    }

    /**
     * 文字转语音，返回 Base64 编码的 PCM 音频（16kHz, 16bit, 单声道）。
     */
    public String synthesize(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        try {
            String curTime = String.valueOf(System.currentTimeMillis() / 1000);
            String param = "{\"aue\":\"raw\",\"auf\":\"audio/L16;rate=16000\",\"voice_name\":\""
                    + voice + "\",\"speed\":\"50\",\"volume\":\"80\",\"pitch\":\"50\",\"engine_type\":\"intp65\"}";
            String paramBase64 = Base64.getEncoder().encodeToString(param.getBytes(StandardCharsets.UTF_8));
            String checkSum = md5(apiKey + curTime + paramBase64);

            String body = "text=" + java.net.URLEncoder.encode(text, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TTS_URL))
                    .header("X-Appid", appId)
                    .header("X-CurTime", curTime)
                    .header("X-Param", paramBase64)
                    .header("X-CheckSum", checkSum)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            log.info("[讯飞TTS] 响应状态: {}, body大小: {} bytes", response.statusCode(),
                    response.body() != null ? response.body().length : 0);

            if (response.statusCode() == 200 && response.body() != null) {
                String contentType = response.headers().firstValue("Content-Type").orElse("");
                if (contentType.contains("audio") || contentType.contains("application/octet-stream")) {
                    // 讯飞返回原始 PCM，加 WAV 头以兼容浏览器播放
                    byte[] wav = pcmToWav(response.body(), 16000, (short) 1, (short) 16);
                    return Base64.getEncoder().encodeToString(wav);
                }
                String respStr = new String(response.body(), StandardCharsets.UTF_8);
                log.warn("[讯飞TTS] 非音频响应: {}", respStr);
            } else {
                log.error("[讯飞TTS] 失败 HTTP {}", response.statusCode());
            }
            return null;
        } catch (Exception e) {
            log.error("[讯飞TTS] 合成异常: {}", e.getMessage());
            return null;
        }
    }

    private static String[] parseCombinedKey(String combinedKey) {
        if (combinedKey == null || combinedKey.isBlank()) {
            throw new IllegalArgumentException("讯飞 TTS combinedKey 不能为空，格式: appid:apikey");
        }
        int idx = combinedKey.indexOf(':');
        if (idx <= 0) {
            throw new IllegalArgumentException("讯飞 TTS Key 格式错误，应为 appid:apikey，如 6497f9b2:your_key");
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

    /**
     * 原始 PCM 数据加 WAV 头（16kHz, 16bit, 单声道）。
     */
    private static byte[] pcmToWav(byte[] pcm, int sampleRate, short channels, short bitsPerSample) {
        int headerSize = 44;
        int dataSize = pcm.length;
        int fileSize = headerSize + dataSize;
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        short blockAlign = (short) (channels * bitsPerSample / 8);

        byte[] wav = new byte[fileSize];
        // RIFF header
        wav[0] = 'R'; wav[1] = 'I'; wav[2] = 'F'; wav[3] = 'F';
        writeInt32(wav, 4, fileSize - 8);
        wav[8] = 'W'; wav[9] = 'A'; wav[10] = 'V'; wav[11] = 'E';
        // fmt chunk
        wav[12] = 'f'; wav[13] = 'm'; wav[14] = 't'; wav[15] = ' ';
        writeInt32(wav, 16, 16);          // chunk size
        writeInt16(wav, 20, (short) 1);   // PCM format
        writeInt16(wav, 22, channels);
        writeInt32(wav, 24, sampleRate);
        writeInt32(wav, 28, byteRate);
        writeInt16(wav, 32, blockAlign);
        writeInt16(wav, 34, bitsPerSample);
        // data chunk
        wav[36] = 'd'; wav[37] = 'a'; wav[38] = 't'; wav[39] = 'a';
        writeInt32(wav, 40, dataSize);
        // copy PCM data
        System.arraycopy(pcm, 0, wav, 44, dataSize);
        return wav;
    }

    private static void writeInt32(byte[] buf, int offset, int value) {
        buf[offset] = (byte) (value & 0xFF);
        buf[offset + 1] = (byte) ((value >> 8) & 0xFF);
        buf[offset + 2] = (byte) ((value >> 16) & 0xFF);
        buf[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }

    private static void writeInt16(byte[] buf, int offset, short value) {
        buf[offset] = (byte) (value & 0xFF);
        buf[offset + 1] = (byte) ((value >> 8) & 0xFF);
    }
}
