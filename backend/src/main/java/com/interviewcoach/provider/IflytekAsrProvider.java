package com.interviewcoach.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 讯飞多语种语音识别 Provider（WebSocket 方式）。
 * 基于大模型多语种语音识别 API: iat.cn-huabei-1.xf-yun.com/v1
 * HMAC-SHA256 签名鉴权，与 TTS 同一套体系。
 */
@Slf4j
public class IflytekAsrProvider {

    private static final String ASR_HOST = "iat.cn-huabei-1.xf-yun.com";
    private static final String ASR_PATH = "/v1";
    private static final int FRAME_SIZE = 1280;
    private static final int FRAME_INTERVAL_MS = 40;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String appId;
    private final String apiKey;
    private final String apiSecret;

    /**
     * @param combinedKey 格式: "appid:apikey:apisecret"，如 "dc4634b7:236f94aeb93bf2be36af835f6c9d0935:ZTUxZmMwYWY3OTQ1ZDZjNWVhY2E2MmZh"
     */
    public IflytekAsrProvider(String combinedKey) {
        String[] parts = parseCombinedKey(combinedKey);
        this.appId = parts[0];
        this.apiKey = parts[1];
        this.apiSecret = parts[2];
    }

    /**
     * 将 PCM/WAV 音频文件识别为文本。
     * 要求：16kHz 16bit 单声道 PCM。
     */
    public String transcribe(Path audioPath) {
        if (audioPath == null || !Files.exists(audioPath)) {
            log.error("[讯飞ASR] 音频文件不存在: {}", audioPath);
            return null;
        }

        try {
            byte[] audioBytes = Files.readAllBytes(audioPath);
            // 如果是 WAV，跳过 WAV 头（前44字节），尽量只使用 PCM 数据
            byte[] pcmBytes = extractPcmFromWav(audioBytes);

            String wsUrl = buildAuthUrl();
            return transcribeViaWs(wsUrl, pcmBytes);
        } catch (Exception e) {
            log.error("[讯飞ASR] 识别异常: {}", e.getMessage());
            return null;
        }
    }

    private String buildAuthUrl() throws Exception {
        String date = generateDateHeader();
        String host = ASR_HOST;
        String tmp = "host: " + host + "\n" +
                     "date: " + date + "\n" +
                     "GET " + ASR_PATH + " HTTP/1.1";

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] sigBytes = mac.doFinal(tmp.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(sigBytes);

        String authOrigin = "api_key=\"" + apiKey + "\", algorithm=\"hmac-sha256\", " +
                           "headers=\"host date request-line\", signature=\"" + signature + "\"";
        String authorization = Base64.getEncoder().encodeToString(authOrigin.getBytes(StandardCharsets.UTF_8));

        return "wss://" + host + ASR_PATH + "?" +
                "authorization=" + authorization +
                "&date=" + date.replace(" ", "%20").replace(",", "%2C") +
                "&host=" + host;
    }

    private String transcribeViaWs(String wsUrl, byte[] pcmBytes) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder finalText = new StringBuilder();
        java.util.concurrent.atomic.AtomicReference<String> errorRef = new java.util.concurrent.atomic.AtomicReference<>();

        HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(URI.create(wsUrl), new java.net.http.WebSocket.Listener() {
                    private java.net.http.WebSocket ws;
                    private int seq = 0;
                    private int pos = 0;

                    @Override
                    public void onOpen(java.net.http.WebSocket webSocket) {
                        this.ws = webSocket;
                        log.info("[讯飞ASR] WebSocket 已连接，音频数据 {} 字节，开始分片发送", pcmBytes.length);
                        webSocket.request(1);

                        // 启动发送线程，按 1280 字节/帧分片发送
                        new Thread(() -> {
                            try {
                                int totalFrames = (int) Math.ceil((double) pcmBytes.length / FRAME_SIZE);
                                for (int i = 0; i < totalFrames; i++) {
                                    int end = Math.min(pos + FRAME_SIZE, pcmBytes.length);
                                    byte[] frame = new byte[end - pos];
                                    System.arraycopy(pcmBytes, pos, frame, 0, frame.length);
                                    pos = end;

                                    int status;
                                    if (i == 0) status = 0;
                                    else if (i == totalFrames - 1) status = 2;
                                    else status = 1;

                                    String frameJson = buildAudioFrameRequest(frame, seq++, status);
                                    ws.sendText(frameJson, true);
                                    Thread.sleep(FRAME_INTERVAL_MS);
                                }
                                log.info("[讯飞ASR] 音频分片发送完成，共 {} 帧，等待识别结果...", totalFrames);
                            } catch (Exception e) {
                                log.error("[讯飞ASR] 发送音频异常: {}", e.getMessage());
                                errorRef.set(e.getMessage());
                                latch.countDown();
                            }
                        }, "iflytek-asr-sender").start();
                    }

                    @Override
                    public java.util.concurrent.CompletionStage<?> onText(java.net.http.WebSocket webSocket, CharSequence data, boolean last) {
                        log.info("[讯飞ASR] 收到消息(len={}): {}", data.length(),
                                data.length() > 150 ? data.subSequence(0, 150) + "..." : data);
                        processResponse(data.toString(), finalText);
                        webSocket.request(1);
                        return null;
                    }

                    @Override
                    public void onError(java.net.http.WebSocket webSocket, Throwable error) {
                        log.error("[讯飞ASR] WebSocket 错误: {}", error.getMessage());
                        errorRef.set(error.getMessage());
                        latch.countDown();
                    }

                    @Override
                    public java.util.concurrent.CompletionStage<?> onClose(java.net.http.WebSocket webSocket, int statusCode, String reason) {
                        log.info("[讯飞ASR] WebSocket 关闭 code={}, reason={}", statusCode, reason);
                        latch.countDown();
                        return null;
                    }
                })
                .join();

        boolean done = latch.await(60, TimeUnit.SECONDS);
        if (!done) log.warn("[讯飞ASR] 超时");

        if (errorRef.get() != null) {
            return null;
        }

        String result = finalText.toString();
        log.info("[讯飞ASR] 最终识别结果: {}", result);
        return result.isEmpty() ? null : result;
    }

    private String buildAudioFrameRequest(byte[] frame, int seq, int status) {
        String audioB64 = Base64.getEncoder().encodeToString(frame);

        // 首帧需要带 parameter，中间和最后一帧只需 header+payload.audio
        if (status == 0) {
            return "{" +
                    "\"header\":{\"app_id\":\"" + appId + "\",\"status\":0}," +
                    "\"parameter\":{\"iat\":{" +
                        "\"domain\":\"slm\"," +
                        "\"language\":\"mul_cn\"," +
                        "\"accent\":\"mandarin\"," +
                        "\"result\":{\"encoding\":\"utf8\",\"compress\":\"raw\",\"format\":\"json\"}" +
                    "}}," +
                    "\"payload\":{\"audio\":{" +
                        "\"encoding\":\"raw\"," +
                        "\"sample_rate\":16000," +
                        "\"channels\":1," +
                        "\"bit_depth\":16," +
                        "\"seq\":" + seq + "," +
                        "\"status\":0," +
                        "\"audio\":\"" + audioB64 + "\"" +
                    "}}" +
                    "}";
        } else {
            return "{" +
                    "\"header\":{\"app_id\":\"" + appId + "\",\"status\":" + status + "}," +
                    "\"payload\":{\"audio\":{" +
                        "\"encoding\":\"raw\"," +
                        "\"sample_rate\":16000," +
                        "\"status\":" + status + "," +
                        "\"seq\":" + seq + "," +
                        "\"audio\":\"" + audioB64 + "\"" +
                    "}}" +
                    "}";
        }
    }

    private void processResponse(String message, StringBuilder finalText) {
        try {
            JsonNode root = objectMapper.readTree(message);
            int code = root.path("header").path("code").asInt(-1);
            int status = root.path("header").path("status").asInt(-1);

            if (code != 0) {
                String msg = root.path("header").path("message").asText("");
                log.warn("[讯飞ASR] 服务端错误 code={}, msg={}", code, msg);
                return;
            }

            JsonNode resultNode = root.path("payload").path("result");
            if (resultNode.isMissingNode()) {
                log.info("[讯飞ASR] 无 payload.result (status={})", status);
                return;
            }

            String textB64 = resultNode.path("text").asText("");
            if (textB64.isEmpty()) {
                log.info("[讯飞ASR] text 字段为空 (status={})", status);
                return;
            }

            // text 是 base64 编码的 JSON，需要解码再解析
            String decodedText = new String(Base64.getDecoder().decode(textB64), StandardCharsets.UTF_8);
            log.info("[讯飞ASR] 解码后的 text: {}", decodedText);
            JsonNode textJson = objectMapper.readTree(decodedText);

            // 从 ws[].cw[].w 提取文字
            StringBuilder segment = new StringBuilder();
            JsonNode ws = textJson.path("ws");
            if (ws.isArray()) {
                for (JsonNode w : ws) {
                    JsonNode cw = w.path("cw");
                    if (cw.isArray() && cw.size() > 0) {
                        String word = cw.get(0).path("w").asText("");
                        if (!word.isEmpty()) {
                            segment.append(word);
                        }
                    }
                }
            }

            if (segment.length() > 0) {
                log.info("[讯飞ASR] 识别文字: {} (status={})", segment, status);
            }

            // status=2 是最终稳定结果，覆盖之前的临时结果
            // 但如果最后一包是空的，保留之前的累积结果
            if (status == 2) {
                if (segment.length() > 0) {
                    finalText.setLength(0);
                    finalText.append(segment);
                }
            } else if (finalText.length() == 0 && segment.length() > 0) {
                // 第一帧有结果时初始化
                finalText.append(segment);
            } else if (segment.length() > 0) {
                // 中间帧追加结果
                finalText.append(segment);
            }
        } catch (Exception e) {
            log.warn("[讯飞ASR] 解析响应失败: {}  raw={}", e.getMessage(),
                    message.length() > 200 ? message.substring(0, 200) : message);
        }
    }

    private static byte[] extractPcmFromWav(byte[] data) {
        // 简单的 WAV 头检测：前4字节是 "RIFF"，从第44字节开始是 PCM 数据
        if (data.length > 44 && data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F') {
            byte[] pcm = new byte[data.length - 44];
            System.arraycopy(data, 44, pcm, 0, pcm.length);
            return pcm;
        }
        return data;
    }

    private String generateDateHeader() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss 'GMT'", java.util.Locale.US);
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
        return sdf.format(new java.util.Date());
    }

    private static String[] parseCombinedKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("讯飞 ASR Key 不能为空，格式: appid:apikey:apisecret");
        }
        String[] parts = key.split(":");
        if (parts.length < 3) {
            throw new IllegalArgumentException("讯飞 ASR Key 格式错误，应为 appid:apikey:apisecret");
        }
        return new String[]{parts[0].trim(), parts[1].trim(), parts[2].trim()};
    }
}
