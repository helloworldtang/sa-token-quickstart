package com.tangtang.satoken.websocket.config;

import com.tangtang.satoken.websocket.handler.AuthWebSocketHandler;
import com.tangtang.satoken.websocket.interceptor.WebSocketAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置类
 *
 * 关键点：
 * 1. 注册 WebSocket 处理器
 * 2. 注册握手拦截器（用于 Token 校验）
 *
 * @author 码骨丹心
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private AuthWebSocketHandler authWebSocketHandler;

    @Autowired
    private WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(authWebSocketHandler, "/ws")
                // 设置允许的来源
                .setAllowedOrigins("*")
                // 注册握手拦截器
                .addInterceptors(webSocketAuthInterceptor);
    }
}
