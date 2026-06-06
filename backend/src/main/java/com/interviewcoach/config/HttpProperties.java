package com.interviewcoach.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * HTTP 客户端配置属性。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.http")
public class HttpProperties {

    /** 连接超时（秒），默认 10 */
    private int connectTimeout = 10;

    /** 读取超时（秒），默认 30 */
    private int readTimeout = 30;

    /** HTTP 代理配置 */
    private ProxyConfig proxy;

    @Data
    public static class ProxyConfig {
        /** 代理主机 */
        private String host;

        /** 代理端口，默认 8080 */
        private int port = 8080;
    }
}
