package com.interviewcoach.websocket;

import com.interviewcoach.service.SpringAiService;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * WebSocket Endpoint 配置器。
 * 通过 Spring ApplicationContext 获取 Bean，解决 JSR 356 Endpoint 无法使用依赖注入的问题。
 */
@Slf4j
public class WebSocketConfigurator extends ServerEndpointConfig.Configurator {

    private static volatile ApplicationContext applicationContext;

    private static ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            synchronized (WebSocketConfigurator.class) {
                if (applicationContext == null) {
                    applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(null);
                }
            }
        }
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        try {
            T endpoint = endpointClass.getDeclaredConstructor().newInstance();

            if (endpoint instanceof InterviewWsEndpoint ws) {
                WebSocketSessionManager sm = WebSocketSessionManager.getInstance();
                if (sm != null) {
                    ws.setSessionManager(sm);
                }

                try {
                    SpringAiService aiService = getApplicationContext().getBean(SpringAiService.class);
                    ws.setSpringAiService(aiService);
                    log.info("[WS] SpringAiService 注入成功");
                } catch (Exception e) {
                    log.warn("[WS] SpringAiService 注入失败，将使用模拟模式: {}", e.getMessage());
                }
            }
            return endpoint;
        } catch (Exception e) {
            log.error("[WS] Endpoint 实例化失败", e);
            throw new InstantiationException("无法创建 Endpoint 实例: " + e.getMessage());
        }
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig sec,
                                HandshakeRequest request,
                                HandshakeResponse response) {
        super.modifyHandshake(sec, request, response);

        var params = request.getParameterMap().get("userId");
        String userId = (params != null && !params.isEmpty()) ? params.get(0) : null;
        if (userId != null) {
            sec.getUserProperties().put(WebSocketSessionManager.KEY_USER_ID, userId);
        }
        response.getHeaders().put("Access-Control-Allow-Origin", java.util.List.of("*"));
    }
}
