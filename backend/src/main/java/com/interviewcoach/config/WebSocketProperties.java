package com.interviewcoach.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * WebSocket 相关配置。
 */
@Data
@ConfigurationProperties(prefix = "app.websocket")
public class WebSocketProperties {

    /** WebSocket 端点路径，不含 context-path。 */
    private String endpoint = "/ws/interview";

    /** 心跳间隔（秒）。 */
    private int heartbeatSeconds = 30;

    /** 允许跨域来源。 */
    private String allowedOrigins = "*";
}
