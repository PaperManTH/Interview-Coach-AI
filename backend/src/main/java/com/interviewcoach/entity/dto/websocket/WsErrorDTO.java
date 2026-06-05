package com.interviewcoach.entity.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 错误信息 DTO。
 * 用于向客户端下发服务端错误信息，配合 {@link MessageType#ERROR} 使用。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WsErrorDTO {

    /** 业务错误码。 */
    private Integer code;

    /** 可读错误信息。 */
    private String message;

    /** 追踪 ID，便于问题定位。 */
    private String traceId;

    /** 发生时间戳（毫秒）。 */
    private Long timestamp;
}
