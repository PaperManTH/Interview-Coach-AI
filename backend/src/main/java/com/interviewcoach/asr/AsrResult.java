package com.interviewcoach.asr;

import java.util.ArrayList;
import java.util.List;

/**
 * ASR 识别结果。
 */
public class AsrResult {

    private String text;
    private boolean isFinal;
    private double confidence;
    private List<Segment> segments = new ArrayList<>();
    private String errorMessage;
    private boolean success;

    public static AsrResult success(String text) {
        AsrResult result = new AsrResult();
        result.success = true;
        result.text = text;
        result.isFinal = true;
        return result;
    }

    public static AsrResult partial(String text) {
        AsrResult result = new AsrResult();
        result.success = true;
        result.text = text;
        result.isFinal = false;
        return result;
    }

    public static AsrResult error(String errorMessage) {
        AsrResult result = new AsrResult();
        result.success = false;
        result.errorMessage = errorMessage;
        return result;
    }

    public static class Segment {
        private String text;
        private Long startTime;
        private Long endTime;

        public static Segment of(String text, Long startTime, Long endTime) {
            Segment s = new Segment();
            s.text = text;
            s.startTime = startTime;
            s.endTime = endTime;
            return s;
        }

        public String getText() { return text; }
        public Long getStartTime() { return startTime; }
        public Long getEndTime() { return endTime; }
    }

    // Getters and Setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public boolean isFinal() { return isFinal; }
    public void setFinal(boolean isFinal) { this.isFinal = isFinal; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public List<Segment> getSegments() { return segments; }
    public void setSegments(List<Segment> segments) { this.segments = segments; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
