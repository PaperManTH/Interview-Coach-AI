package com.interviewcoach.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket 配置类。
 * <p>
 * 显式注册 JSR 356 WebSocket 端点。
 */
@Slf4j
@Configuration
public class WebSocketConfig {

    /**
     * 注册 ServerEndpointExporter，并显式指定要注册的端点类。
     * 这种方式确保端点被正确注册，不依赖类路径扫描。
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        log.info("[WS] 初始化 ServerEndpointExporter");
        ServerEndpointExporter exporter = new ServerEndpointExporter();
        // 显式注册 InterviewWsEndpoint
        exporter.setAnnotatedEndpointClasses(InterviewWsEndpoint.class);
        log.info("[WS] 已注册 InterviewWsEndpoint");
        return exporter;
    }
}