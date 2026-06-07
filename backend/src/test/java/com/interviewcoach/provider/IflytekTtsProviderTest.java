package com.interviewcoach.provider;

import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class IflytekTtsProviderTest {

    private static final String TEST_TEXT_CN = "你好，欢迎使用讯飞超拟人语音合成服务。";
    private static final String TEST_TEXT_EN = "Hello, welcome to iFlytek Super TTS Service.";
    
    private static final String COMBINED_KEY = System.getenv().getOrDefault("IFLYTEK_COMBINED_KEY", "appId:apiKey:apiSecret");

    @Test
    public void testSuperHumanChineseVoiceSynthesis() throws Exception {
        System.out.println("=== 测试讯飞超拟人中文发音人 ===");
        
        String[] voices = {
            "x5_lingfeiyi_flow",
            "x5_lingxiaoxuan_flow",
            "x5_lingxiaoyue_flow",
            "x5_lingyuyan_flow",
            "x5_lingyuzhao_flow"
        };
        
        for (String voice : voices) {
            System.out.println("\n--- 测试发音人: " + voice + " ---");
            testSynthesis(voice, TEST_TEXT_CN);
        }
    }

    @Test
    public void testSuperHumanEnglishVoiceSynthesis() throws Exception {
        System.out.println("=== 测试讯飞超拟人英文发音人 ===");
        
        String[] voices = {
            "x5_EnUs_Lila_flow",
            "x5_EnUs_Grant_flow"
        };
        
        for (String voice : voices) {
            System.out.println("\n--- 测试发音人: " + voice + " ---");
            testSynthesis(voice, TEST_TEXT_EN);
        }
    }

    @Test
    public void testDefaultVoice() throws Exception {
        System.out.println("=== 测试默认发音人 ===");
        IflytekTtsProvider provider = new IflytekTtsProvider(COMBINED_KEY, null);
        String result = provider.synthesize(TEST_TEXT_CN);
        
        if (result != null && !result.isEmpty()) {
            System.out.println("✓ 默认发音人合成成功，音频长度: " + result.length() + " 字符");
            saveAudioFile(result, "default_voice.wav");
        } else {
            System.out.println("✗ 默认发音人合成失败");
        }
    }

    @Test
    public void testLongTextSynthesis() throws Exception {
        System.out.println("=== 测试长文本合成 ===");
        
        String longText = "今天天气非常好，阳光明媚，微风轻拂。我来到公园散步，看到了许多美丽的花朵和欢快的小鸟。" +
                         "公园里的人们有的在跑步，有的在打太极拳，还有的在带着孩子玩耍。" +
                         "这真是一个美好的早晨，让人心情愉悦。";
        
        IflytekTtsProvider provider = new IflytekTtsProvider(COMBINED_KEY, "x5_lingfeiyi_flow");
        String result = provider.synthesize(longText);
        
        if (result != null && !result.isEmpty()) {
            System.out.println("✓ 长文本合成成功，文本长度: " + longText.length() + " 字，音频长度: " + result.length() + " 字符");
            saveAudioFile(result, "long_text.wav");
        } else {
            System.out.println("✗ 长文本合成失败");
        }
    }

    @Test
    public void testSpecialCharacters() throws Exception {
        System.out.println("=== 测试特殊字符处理 ===");
        
        String specialText = "价格是¥199.99，时间是2024-01-15，温度是30°C。" +
                           "联系方式：电话123-4567-8900，邮箱test@example.com。";
        
        IflytekTtsProvider provider = new IflytekTtsProvider(COMBINED_KEY, "x5_lingfeiyi_flow");
        String result = provider.synthesize(specialText);
        
        if (result != null && !result.isEmpty()) {
            System.out.println("✓ 特殊字符处理成功");
            saveAudioFile(result, "special_chars.wav");
        } else {
            System.out.println("✗ 特殊字符处理失败");
        }
    }

    private void testSynthesis(String voice, String text) {
        try {
            IflytekTtsProvider provider = new IflytekTtsProvider(COMBINED_KEY, voice);
            String result = provider.synthesize(text);
            
            if (result != null && !result.isEmpty()) {
                System.out.println("✓ 合成成功，音频长度: " + result.length() + " 字符");
                saveAudioFile(result, voice + ".wav");
            } else {
                System.out.println("✗ 合成失败");
            }
        } catch (Exception e) {
            System.out.println("✗ 异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveAudioFile(String base64Audio, String fileName) {
        try {
            byte[] audioBytes = Base64.getDecoder().decode(base64Audio);
            String outputPath = "target/" + fileName;
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                fos.write(audioBytes);
            }
            System.out.println("  → 音频已保存: " + outputPath);
        } catch (Exception e) {
            System.out.println("  ✗ 保存失败: " + e.getMessage());
        }
    }

    @Test
    public void testKeyParsing() {
        System.out.println("=== 测试密钥解析 ===");
        
        try {
            new IflytekTtsProvider(COMBINED_KEY, "x5_lingfeiyi_flow");
            System.out.println("✓ 密钥格式正确，解析成功");
        } catch (Exception e) {
            System.out.println("✗ 解析失败: " + e.getMessage());
        }
    }

    @Test
    public void testInvalidKeyFormat() {
        System.out.println("=== 测试无效密钥格式 ===");
        
        String[] invalidKeys = {
            "",
            "only_appid",
            "appid:apikey",
            null
        };
        
        for (String key : invalidKeys) {
            try {
                new IflytekTtsProvider(key, "x5_lingfeiyi_flow");
                System.out.println("✗ 应该抛出异常，但未抛出");
            } catch (IllegalArgumentException e) {
                System.out.println("✓ 正确抛出异常: " + e.getMessage());
            }
        }
    }

    @Test
    public void testSuperHumanAuthDebug() throws Exception {
        System.out.println("\n=== 详细调试超拟人鉴权流程 ===");
        
        String host = "cbm01.cn-huabei-1.xf-yun.com";
        String path = "/v1/private/mcd9m97e6";
        String apiKey = System.getenv().getOrDefault("IFLYTEK_API_KEY", "your-api-key");
        String apiSecret = System.getenv().getOrDefault("IFLYTEK_API_SECRET", "your-api-secret");
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss 'GMT'", java.util.Locale.US);
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
        String date = sdf.format(new java.util.Date());
        System.out.println("Step 1 - date: [" + date + "]");
        
        String tmp = "host: " + host + "\n" +
                     "date: " + date + "\n" +
                     "GET " + path + " HTTP/1.1";
        System.out.println("\nStep 2 - tmp:\n" + tmp);
        
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(
                apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] sigBytes = mac.doFinal(tmp.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(sigBytes);
        System.out.println("\nStep 3 - signature: [" + signature + "]");
        
        String authorizationOrigin = "hmac username=\"" + apiKey + 
                "\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"" + signature + "\"";
        System.out.println("\nStep 4 - authorizationOrigin: [" + authorizationOrigin + "]");
        
        String authorization = Base64.getEncoder().encodeToString(
                authorizationOrigin.getBytes(StandardCharsets.UTF_8));
        System.out.println("\nStep 5 - authorization: [" + authorization + "]");
        
        String url = "wss://" + host + path + "?" +
                "authorization=" + authorization +
                "&date=" + date.replace(" ", "%20").replace(",", "%2C") +
                "&host=" + host;
        
        System.out.println("\nStep 6 - 最终 URL:\n" + url);
        System.out.println("\n=== 鉴权调试完成 ===");
    }

    @Test
    public void testSuperHumanWsConnection() throws Exception {
        System.out.println("\n=== 测试超拟人 WebSocket 连接 ===");
        
        String host = "cbm01.cn-huabei-1.xf-yun.com";
        String path = "/v1/private/mcd9m97e6";
        String apiKey = System.getenv().getOrDefault("IFLYTEK_API_KEY", "your-api-key");
        String apiSecret = System.getenv().getOrDefault("IFLYTEK_API_SECRET", "your-api-secret");
        String appId = System.getenv().getOrDefault("IFLYTEK_APP_ID", "your-app-id");
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss 'GMT'", java.util.Locale.US);
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
        String date = sdf.format(new java.util.Date());
        
        String tmp = "host: " + host + "\n" +
                     "date: " + date + "\n" +
                     "GET " + path + " HTTP/1.1";
        
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(
                apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] sigBytes = mac.doFinal(tmp.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(sigBytes);
        
        String authorizationOrigin = "hmac username=\"" + apiKey + 
                "\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"" + signature + "\"";
        String authorization = Base64.getEncoder().encodeToString(
                authorizationOrigin.getBytes(StandardCharsets.UTF_8));
        
        String wsUrl = "wss://" + host + path + "?" +
                "authorization=" + authorization +
                "&date=" + date.replace(" ", "%20").replace(",", "%2C") +
                "&host=" + host;
        
        System.out.println("WebSocket URL: " + wsUrl);
        System.out.println("\n尝试连接...");
        
        java.util.concurrent.CompletableFuture<String> resultFuture = new java.util.concurrent.CompletableFuture<>();
        
        java.net.http.WebSocket ws = java.net.http.HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(java.net.URI.create(wsUrl), new java.net.http.WebSocket.Listener() {
                    @Override
                    public void onOpen(java.net.http.WebSocket webSocket) {
                        System.out.println("✓ WebSocket 连接成功！");
                        
                        String textToSynthesize = "你好，欢迎使用讯飞超拟人语音合成";
                        String encodedText = Base64.getEncoder().encodeToString(
                                textToSynthesize.getBytes(StandardCharsets.UTF_8));
                        
                        String requestJson = "{" +
                                "\"header\":{\"app_id\":\"" + appId + "\",\"status\":2}," +
                                "\"parameter\":{" +
                                    "\"oral\":{\"spark_assist\":1,\"oral_level\":\"mid\"}," +
                                    "\"tts\":{" +
                                        "\"vcn\":\"x5_lingfeiyi_flow\"," +
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
                        
                        System.out.println("发送请求: " + requestJson);
                        webSocket.sendText(requestJson, true);
                        webSocket.request(1);
                    }
                    
                    @Override
                    public java.util.concurrent.CompletionStage<?> onText(java.net.http.WebSocket webSocket, CharSequence data, boolean last) {
                        System.out.println("收到消息: " + data.length() + " 字符");
                        webSocket.request(1);
                        return null;
                    }
                    
                    @Override
                    public java.util.concurrent.CompletionStage<?> onBinary(java.net.http.WebSocket webSocket, java.nio.ByteBuffer data, boolean last) {
                        System.out.println("收到二进制消息: " + data.remaining() + " 字节");
                        webSocket.request(1);
                        return null;
                    }
                    
                    @Override
                    public void onError(java.net.http.WebSocket webSocket, Throwable error) {
                        System.out.println("✗ WebSocket 错误: " + error.getMessage());
                        error.printStackTrace();
                    }
                    
                    @Override
                    public java.util.concurrent.CompletionStage<?> onClose(java.net.http.WebSocket webSocket, int statusCode, String reason) {
                        System.out.println("WebSocket 关闭: code=" + statusCode + ", reason=" + reason);
                        resultFuture.complete("closed");
                        return null;
                    }
                })
                .join();
        
        boolean completed = resultFuture.orTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .handle((r, e) -> {
                    if (e != null) {
                        System.out.println("超时或异常: " + e.getMessage());
                    }
                    return true;
                }).join();
        
        System.out.println("\n=== 连接测试完成 ===");
    }

    @Test
    public void testEmptyInput() {
        System.out.println("=== 测试空输入 ===");
        
        IflytekTtsProvider provider = new IflytekTtsProvider(COMBINED_KEY, "x5_lingfeiyi_flow");
        
        String result1 = provider.synthesize(null);
        System.out.println("null 输入: " + (result1 == null ? "✓ 正确返回null" : "✗ 应该返回null"));
        
        String result2 = provider.synthesize("");
        System.out.println("空字符串输入: " + (result2 == null ? "✓ 正确返回null" : "✗ 应该返回null"));
        
        String result3 = provider.synthesize("   ");
        System.out.println("空白字符串输入: " + (result3 == null ? "✓ 正确返回null" : "✗ 应该返回null"));
    }
}