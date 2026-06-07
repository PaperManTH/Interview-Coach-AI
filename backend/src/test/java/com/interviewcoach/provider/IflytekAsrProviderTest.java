package com.interviewcoach.provider;

import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IflytekAsrProviderTest {

    private static final String COMBINED_KEY = System.getenv().getOrDefault("IFLYTEK_COMBINED_KEY", "appId:apiKey:apiSecret");

    @Test
    public void testIflytekAsr() throws Exception {
        System.out.println("\n=== 测试讯飞多语种语音识别 ===");
        System.out.println("Key: " + COMBINED_KEY);

        // 生成一个简单的测试音频文件（16kHz, 16bit, mono PCM WAV）
        Path testAudio = generateTestWav();
        System.out.println("测试音频: " + testAudio + " (" + testAudio.toFile().length() + " bytes)");

        try {
            IflytekAsrProvider provider = new IflytekAsrProvider(COMBINED_KEY);
            String result = provider.transcribe(testAudio);
            System.out.println("识别结果: " + result);
        } catch (Exception e) {
            System.out.println("异常: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== 测试完成 ===");
    }

    private Path generateTestWav() throws Exception {
        int sampleRate = 16000;
        int durationSeconds = 2;
        int numSamples = sampleRate * durationSeconds;
        byte[] audioData = new byte[numSamples * 2]; // 16-bit = 2 bytes per sample

        // 生成一个简单的 440Hz 正弦波（可听的声音）
        for (int i = 0; i < numSamples; i++) {
            double sample = Math.sin(2 * Math.PI * 440 * i / sampleRate) * 0.3;
            short s = (short) (sample * 32767);
            audioData[i * 2] = (byte) (s & 0xFF);
            audioData[i * 2 + 1] = (byte) ((s >> 8) & 0xFF);
        }

        AudioFormat format = new AudioFormat(
                sampleRate,
                16,
                1,
                true,
                false
        );
        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
        AudioInputStream ais = new AudioInputStream(bais, format, numSamples);

        Path wavPath = Paths.get("target", "test_audio_16k.wav");
        File outFile = wavPath.toFile();
        outFile.getParentFile().mkdirs();
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outFile);
        return wavPath;
    }

    @Test
    public void testKeyParsing() {
        System.out.println("\n=== 测试 ASR Key 解析 ===");
        try {
            IflytekAsrProvider provider = new IflytekAsrProvider(COMBINED_KEY);
            System.out.println("✓ Key 解析成功");
        } catch (Exception e) {
            System.out.println("✗ 解析失败: " + e.getMessage());
        }

        // 测试错误格式
        try {
            new IflytekAsrProvider("invalid");
            System.out.println("✗ 应该抛出异常");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ 正确拒绝无效格式: " + e.getMessage());
        }
    }
}
