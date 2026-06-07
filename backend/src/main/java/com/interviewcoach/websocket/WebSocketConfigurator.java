package com.interviewcoach.websocket;

import com.interviewcoach.service.AsrService;
import com.interviewcoach.service.ConversationService;
import com.interviewcoach.service.SpringAiService;
import com.interviewcoach.service.TtsService;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * WebSocket Endpoint 配置器。
 */
@Slf4j
public class WebSocketConfigurator extends ServerEndpointConfig.Configurator {

    private static volatile ApplicationContext applicationContext;

    private static ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            synchronized (WebSocketConfigurator.class) {
                if (applicationContext == null) {
                    // 尝试从 ServletContext 获取，若失败则返回 null（后续会在 modifyHandshake 中设置）
                    try {
                        applicationContext = WebApplicationContextUtils.getWebApplicationContext(null);
                    } catch (Exception e) {
                        log.warn("[WS] 无法从 ServletContext 获取 ApplicationContext");
                    }
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

                try {
                    AsrService asrService = getApplicationContext().getBean(AsrService.class);
                    ws.setAsrService(asrService);
                    log.info("[WS] AsrService 注入成功");
                } catch (Exception e) {
                    log.warn("[WS] AsrService 注入失败: {}", e.getMessage());
                }

                try {
                    TtsService ttsService = getApplicationContext().getBean(TtsService.class);
                    ws.setTtsService(ttsService);
                    log.info("[WS] TtsService 注入成功");
                } catch (Exception e) {
                    log.warn("[WS] TtsService 注入失败: {}", e.getMessage());
                }

                try {
                    ConversationService conversationService = getApplicationContext().getBean(ConversationService.class);
                    ws.setConversationService(conversationService);
                    log.info("[WS] ConversationService 注入成功");
                } catch (Exception e) {
                    log.warn("[WS] ConversationService 注入失败: {}", e.getMessage());
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
