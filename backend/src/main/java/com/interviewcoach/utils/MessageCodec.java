package com.interviewcoach.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.interviewcoach.entity.dto.websocket.MessageDTO;

import java.io.IOException;

/**
 * WebSocket 消息编解码工具。
 * 内部持有一个线程安全的 Jackson {@link ObjectMapper} 单例。
 */
public final class MessageCodec {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private MessageCodec() {
    }

    /**
     * 将 {@link MessageDTO} 序列化为 JSON 字符串。
     */
    public static String serialize(MessageDTO msg) throws JsonProcessingException {
        return MAPPER.writeValueAsString(msg);
    }

    /**
     * 将 JSON 字符串反序列化为 {@link MessageDTO}。
     */
    public static MessageDTO deserialize(String raw) throws IOException {
        return MAPPER.readValue(raw, MessageDTO.class);
    }

    /**
     * 通用：任意对象 -> JSON 字符串。
     */
    public static String toJson(Object obj) throws JsonProcessingException {
        return MAPPER.writeValueAsString(obj);
    }

    /**
     * 通用：JSON 字符串 -> 指定类型对象。
     */
    public static <T> T fromJson(String raw, Class<T> type) throws IOException {
        return MAPPER.readValue(raw, type);
    }

    /**
     * 解析 JSON 字符串为 JsonNode，用于提取特定字段。
     */
    public static JsonNode parseJson(String raw) throws IOException {
        return MAPPER.readTree(raw);
    }

    /**
     * 暴露内部 ObjectMapper，便于 Spring 环境下统一配置或扩展。
     */
    public static ObjectMapper objectMapper() {
        return MAPPER;
    }
}
