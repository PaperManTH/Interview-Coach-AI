package com.interviewcoach.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;

/**
 * RestClient 全局配置 — 超时 + HTTP 代理（解决 GitHub API 无法直连问题）。
 */
@Slf4j
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder(HttpProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 超时
        factory.setConnectTimeout(Duration.ofSeconds(props.getConnectTimeout()));
        factory.setReadTimeout(Duration.ofSeconds(props.getReadTimeout()));

        // HTTP 代理
        if (props.getProxy() != null
                && props.getProxy().getHost() != null
                && !props.getProxy().getHost().isBlank()) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(props.getProxy().getHost(), props.getProxy().getPort()));
            factory.setProxy(proxy);
            log.info("[HTTP] 代理已启用: {}:{}", props.getProxy().getHost(), props.getProxy().getPort());
        } else {
            log.info("[HTTP] 直连模式 (无代理)");
        }

        return RestClient.builder().requestFactory(factory);
    }
}
