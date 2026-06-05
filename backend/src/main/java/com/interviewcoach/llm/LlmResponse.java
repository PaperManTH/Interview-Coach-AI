package com.interviewcoach.llm;

import java.util.List;

/**
 * LLM 对话响应。
 */
public class LlmResponse {

    private String text;
    private boolean isFinal;
    private String model;
    private Usage usage;
    private List<LlmContext.Message> messages;
    private String errorMessage;
    private boolean success;

    public static LlmResponse success(String text) {
        LlmResponse response = new LlmResponse();
        response.success = true;
        response.text = text;
        response.isFinal = true;
        return response;
    }

    public static LlmResponse partial(String text) {
        LlmResponse response = new LlmResponse();
        response.success = true;
        response.text = text;
        response.isFinal = false;
        return response;
    }

    public static LlmResponse error(String errorMessage) {
        LlmResponse response = new LlmResponse();
        response.success = false;
        response.errorMessage = errorMessage;
        return response;
    }

    /**
     * 令牌使用情况。
     */
    public record Usage(int promptTokens, int completionTokens, int totalTokens) {}

    // Getters and Setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public boolean isFinal() { return isFinal; }
    public void setFinal(boolean isFinal) { this.isFinal = isFinal; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Usage getUsage() { return usage; }
    public void setUsage(Usage usage) { this.usage = usage; }
    public List<LlmContext.Message> getMessages() { return messages; }
    public void setMessages(List<LlmContext.Message> messages) { this.messages = messages; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
