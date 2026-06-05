package com.interviewcoach.llm;

import java.util.ArrayList;
import java.util.List;

/**
 * LLM 对话上下文。
 * 包含对话所需的配置和历史记录。
 */
public class LlmContext {

    private String model = "gpt-4o-mini";
    private Double temperature = 0.7;
    private Integer maxTokens = 4096;
    private List<Message> messages = new ArrayList<>();

    private LlmContext() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final LlmContext context = new LlmContext();

        public Builder model(String model) {
            context.model = model;
            return this;
        }

        public Builder temperature(Double temperature) {
            context.temperature = temperature;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            context.maxTokens = maxTokens;
            return this;
        }

        public Builder messages(List<Message> messages) {
            context.messages = messages;
            return this;
        }

        public Builder addMessage(Message message) {
            context.messages.add(message);
            return this;
        }

        public LlmContext build() {
            return context;
        }
    }

    /**
     * 对话消息。
     */
    public record Message(String role, String content) {
        public static Message user(String content) {
            return new Message("user", content);
        }
        public static Message assistant(String content) {
            return new Message("assistant", content);
        }
        public static Message system(String content) {
            return new Message("system", content);
        }
    }

    // Getters
    public String getModel() { return model; }
    public Double getTemperature() { return temperature; }
    public Integer getMaxTokens() { return maxTokens; }
    public List<Message> getMessages() { return messages; }
}
