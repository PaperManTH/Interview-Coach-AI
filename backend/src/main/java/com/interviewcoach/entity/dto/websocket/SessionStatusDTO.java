package com.interviewcoach.entity.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话状态 DTO。
 * 描述一个 WebSocket 会话的在线/连接/心跳与消息计数信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionStatusDTO {

    /** 会话 ID。 */
    private String sessionId;

    /** 关联用户 ID，可为 null 表示匿名。 */
    private String userId;

    /** 是否在线。 */
    private boolean online;

    /** 连接建立时间（毫秒）。 */
    private Long connectedAt;

    /** 最后一次心跳时间（毫秒）。 */
    private Long lastHeartbeatAt;

    /** 已处理消息数量。 */
    private long messageCount;
}
