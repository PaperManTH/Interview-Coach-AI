package com.interviewcoach.tts;

/**
 * TTS 合成上下文。
 * 包含合成所需的配置和元数据。
 */
public class TtsContext {

    private String voice = "default";
    private String language = "zh_CN";
    private Double rate = 1.0;
    private Double pitch = 1.0;
    private Integer sampleRate = 24000;
    private String audioFormat = "mp3";

    private TtsContext() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final TtsContext context = new TtsContext();

        public Builder voice(String voice) {
            context.voice = voice;
            return this;
        }

        public Builder language(String language) {
            context.language = language;
            return this;
        }

        public Builder rate(Double rate) {
            context.rate = rate;
            return this;
        }

        public Builder pitch(Double pitch) {
            context.pitch = pitch;
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

        public TtsContext build() {
            return context;
        }
    }

    // Getters
    public String getVoice() { return voice; }
    public String getLanguage() { return language; }
    public Double getRate() { return rate; }
    public Double getPitch() { return pitch; }
    public Integer getSampleRate() { return sampleRate; }
    public String getAudioFormat() { return audioFormat; }
}
