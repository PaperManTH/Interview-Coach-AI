package com.interviewcoach.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class IflytekTtsProvider {

    private static final String TTS_HOST = "cbm01.cn-huabei-1.xf-yun.com";
    private static final String TTS_PATH = "/v1/private/mcd9m97e6";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String appId;
    private final String apiKey;
    private final String apiSecret;
    private final String voice;

    public IflytekTtsProvider(String combinedKey, String voice) {
        String[] parts = parseCombinedKey(combinedKey);
        this.appId = parts[0];
        this.apiKey = parts[1];
        this.apiSecret = parts[2];
        this.voice = voice != null && !voice.isBlank() ? voice : "x5_lingfeiyi_flow";
        log.info("[讯飞超拟人TTS] 初始化 appId={}, apiKey={}, apiSecret={}, voice={}",
                appId, maskKey(apiKey), maskKey(apiSecret), this.voice);
    }

    public String synthesize(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            String wsUrl = buildAuthUrl();
            log.info("[讯飞超拟人TTS] 连接 URL={}", wsUrl);

            byte[] result = synthesizeViaWs(wsUrl, text);
            if (result != null && result.length > 0) {
                byte[] wav = pcmToWav(result, 16000, (short) 1, (short) 16);
                return Base64.getEncoder().encodeToString(wav);
            }
            return null;
        } catch (Exception e) {
            log.error("[讯飞超拟人TTS] 合成异常: {}", e.getMessage(), e);
            return null;
        }
    }

    private String buildAuthUrl() throws Exception {
        String date = generateDateHeader();
        String host = TTS_HOST;
        String tmp = "host: " + host + "\n" +
                     "date: " + date + "\n" +
                     "GET " + TTS_PATH + " HTTP/1.1";

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] sigBytes = mac.doFinal(tmp.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(sigBytes);

        String authOrigin = "hmac username=\"" + apiKey + "\", algorithm=\"hmac-sha256\", " +
                "headers=\"host date request-line\", signature=\"" + signature + "\"";
        String authorization = Base64.getEncoder().encodeToString(authOrigin.getBytes(StandardCharsets.UTF_8));

        return "wss://" + host + TTS_PATH + "?" +
                "authorization=" + authorization +
                "&date=" + date.replace(" ", "%20").replace(",", "%2C") +
                "&host=" + host;
    }

    private byte[] synthesizeViaWs(String wsUrl, String text) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        java.io.ByteArrayOutputStream audioBuffer = new java.io.ByteArrayOutputStream();

        HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(URI.create(wsUrl), new java.net.http.WebSocket.Listener() {
                    private StringBuilder buffer = new StringBuilder();

                    @Override
                    public void onOpen(java.net.http.WebSocket webSocket) {
                        log.info("[讯飞超拟人TTS] WebSocket 已连接");
                        try {
                            String requestJson = buildRequest(text);
                            webSocket.sendText(requestJson, true);
                            webSocket.request(1);
                        } catch (Exception e) {
                            log.error("[讯飞超拟人TTS] 发送失败: {}", e.getMessage());
                            latch.countDown();
                        }
                    }

                    @Override
                    public java.util.concurrent.CompletionStage<?> onText(java.net.http.WebSocket webSocket, CharSequence data, boolean last) {
                        buffer.append(data);
                        if (last) {
                            processWsMessage(buffer.toString(), audioBuffer, latch);
                            buffer.setLength(0);
                        }
                        webSocket.request(1);
                        return null;
                    }

                    @Override
                    public void onError(java.net.http.WebSocket webSocket, Throwable error) {
                        log.error("[讯飞超拟人TTS] WebSocket 错误: {}", error.getMessage());
                        latch.countDown();
                    }

                    @Override
                    public java.util.concurrent.CompletionStage<?> onClose(java.net.http.WebSocket webSocket, int statusCode, String reason) {
                        log.info("[讯飞超拟人TTS] WebSocket 关闭 code={}, reason={}", statusCode, reason);
                        latch.countDown();
                        return null;
                    }
                })
                .join();

        boolean done = latch.await(30, TimeUnit.SECONDS);
        if (!done) {
            log.warn("[讯飞超拟人TTS] 超时");
        }

        byte[] result = audioBuffer.toByteArray();
        if (result.length == 0) return null;
        return result;
    }

    private void processWsMessage(String message, java.io.ByteArrayOutputStream audioBuffer, CountDownLatch latch) {
        try {
            JsonNode root = objectMapper.readTree(message);
            JsonNode header = root.path("header");
            int code = header.path("code").asInt(-1);
            if (code != 0) {
                String msg = header.path("message").asText("未知错误");
                log.warn("[讯飞超拟人TTS] 服务端错误 code={}, msg={}", code, msg);
                latch.countDown();
                return;
            }
            JsonNode payload = root.path("payload");
            if (payload != null && !payload.isNull()) {
                JsonNode audioNode = payload.path("audio");
                String audio = audioNode.path("audio").asText("");
                int status = header.path("status").asInt(-1);
                if (!audio.isEmpty()) {
                    byte[] audioBytes = Base64.getDecoder().decode(audio);
                    audioBuffer.write(audioBytes);
                    if (status == 2) {
                        log.info("[讯飞超拟人TTS] 收到完整音频数据, length={}", audioBuffer.size());
                        latch.countDown();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[讯飞超拟人TTS] 解析响应失败: {}", e.getMessage());
        }
    }

    private String buildRequest(String text) {
        String encodedText = Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
        return "{" +
                "\"header\":{\"app_id\":\"" + appId + "\",\"status\":2}," +
                "\"parameter\":{" +
                    "\"oral\":{\"spark_assist\":1,\"oral_level\":\"mid\"}," +
                    "\"tts\":{" +
                        "\"vcn\":\"" + voice + "\"," +
                        "\"speed\":50," +
                        "\"volume\":50," +
                        "\"pitch\":50," +
                        "\"bgs\":0," +
                        "\"audio\":{\"encoding\":\"raw\",\"sample_rate\":16000,\"channels\":1,\"bit_depth\":16}," +
                        "\"pybuf\":{\"encoding\":\"utf8\",\"compress\":\"raw\",\"format\":\"plain\"}" +
                    "}" +
                "}," +
                "\"payload\":{" +
                    "\"text\":{\"encoding\":\"utf8\",\"compress\":\"raw\",\"format\":\"plain\",\"status\":2,\"seq\":0,\"text\":\"" + encodedText + "\"}" +
                "}" +
                "}";
    }

    private String generateDateHeader() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss 'GMT'", java.util.Locale.US);
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
        return sdf.format(new java.util.Date());
    }

    private static String[] parseCombinedKey(String key) {
        if (key == null || key.isBlank())
            throw new IllegalArgumentException("格式: appid:apikey:apisecret");
        String[] parts = key.split(":");
        if (parts.length < 3)
            throw new IllegalArgumentException("格式错误，应为 appid:apikey:apisecret");
        return new String[]{parts[0].trim(), parts[1].trim(), parts[2].trim()};
    }

    private static String maskKey(String k) {
        if (k == null || k.length() <= 6) return "****";
        return k.substring(0, 3) + "****" + k.substring(k.length() - 3);
    }

    private static byte[] pcmToWav(byte[] pcm, int sr, short ch, short bps) {
        byte[] wav = new byte[44 + pcm.length];
        wav[0]='R';wav[1]='I';wav[2]='F';wav[3]='F';
        writeInt32(wav,4,wav.length-8);
        wav[8]='W';wav[9]='A';wav[10]='V';wav[11]='E';
        wav[12]='f';wav[13]='m';wav[14]='t';wav[15]=' ';
        writeInt32(wav,16,16); writeInt16(wav,20,(short)1);
        writeInt16(wav,22,ch); writeInt32(wav,24,sr);
        writeInt32(wav,28,sr*ch*bps/8); writeInt16(wav,32,(short)(ch*bps/8));
        writeInt16(wav,34,bps); wav[36]='d';wav[37]='a';wav[38]='t';wav[39]='a';
        writeInt32(wav,40,pcm.length);
        System.arraycopy(pcm,0,wav,44,pcm.length);
        return wav;
    }
    private static void writeInt32(byte[] b, int o, int v){b[o]=(byte)v;b[o+1]=(byte)(v>>8);b[o+2]=(byte)(v>>16);b[o+3]=(byte)(v>>24);}
    private static void writeInt16(byte[] b, int o, short v){b[o]=(byte)v;b[o+1]=(byte)(v>>8);}
}