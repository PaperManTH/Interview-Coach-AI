package com.interviewcoach.entity.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * WebSocket 通用消息 DTO。
 * 支持文本/心跳/ACK/语音分片等多种类型，字段按职责清晰划分。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

    /** 消息唯一 ID，用于 ACK 回执与去重。 */
    private String id;

    /** 消息类型，见 {@link MessageType}。 */
    private MessageType type;

    /** 发送方标识（用户 ID 或 "system"/"assistant"）。 */
    private String sender;

    /** 接收方标识，可为 null 表示广播。 */
    private String receiver;

    /** 消息正文。 */
    private String content;

    /** 发送时间戳（毫秒）。 */
    private Long timestamp;

    /** 所属会话 ID。 */
    private String sessionId;

    /** 分片序号（从 0 开始），非分片场景为 null。 */
    private Long chunkIndex;

    /** 总分片数，非分片场景为 null。 */
    private Long totalChunks;

    /** 扩展元数据，便于后续业务扩展。 */
    private Map<String, Object> metadata;

    /**
     * 便捷方法：构造一条普通文本消息。
     */
    public static MessageDTO text(String sessionId, String sender, String content) {
        return MessageDTO.builder()
                .id(UUID.randomUUID().toString())
                .type(MessageType.TEXT)
                .sender(sender)
                .content(content)
                .timestamp(System.currentTimeMillis())
                .sessionId(sessionId)
                .build();
    }

    /**
     * 便捷方法：构造一条 ACK 回执。
     */
    public static MessageDTO ack(String id) {
        return MessageDTO.builder()
                .id(id)
                .type(MessageType.ACK)
                .sender("system")
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
