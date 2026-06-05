package com.interviewcoach.asr;

/**
 * ASR 识别上下文。
 * 包含识别所需的配置和元数据。
 */
public class AsrContext {

    private String language = "zh_CN";
    private String model = "general";
    private Integer sampleRate = 16000;
    private String audioFormat = "pcm";
    private String userId;

    private AsrContext() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AsrContext context = new AsrContext();

        public Builder language(String language) {
            context.language = language;
            return this;
        }

        public Builder model(String model) {
            context.model = model;
            return this;
        }

        public Builder sampleRate(Integer sampleRate) {
            context.sampleRate = sampleRate;
            return this;
        }

        public Builder audioFormat(String audioFormat) {
            context.audioFormat = audioFormat;
            return this;
        }

        public Builder userId(String userId) {
            context.userId = userId;
            return this;
        }

        public AsrContext build() {
            return context;
        }
    }

    // Getters
    public String getLanguage() { return language; }
    public String getModel() { return model; }
    public Integer getSampleRate() { return sampleRate; }
    public String getAudioFormat() { return audioFormat; }
    public String getUserId() { return userId; }
}
