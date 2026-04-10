package com.tangtang.satoken.websocket.interceptor;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器
 *
 * 核心职责：
 * 在 WebSocket 连接建立时，校验 Token 并建立会话关联
 *
 * 认证流程：
 * 1. 从请求参数或 Header 获取 Token
 * 2. 使用 Sa-Token 校验 Token 有效性
 * 3. 将用户信息存储到握手属性中，供 Handler 使用
 *
 * 支持 3 种 Token 传递方式：
 * - URL 参数: ws://host/ws?satoken=xxx
 * - Header: satoken: xxx
 * - Header: Authorization: Bearer xxx
 *
 * @author 码骨丹心
 */
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    @Value("${sa-token.token-name:satoken}")
    private String tokenName;

    /**
     * 握手前执行 - 校验 Token
     *
     * @return true 允许握手，false 拒绝握手
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        // 1. 从请求中获取 Token
        String token = getToken(request);

        if (token == null || token.isEmpty()) {
            log.warn("[WebSocket] 握手失败: 未提供 Token");
            response.getBody().write("{\"code\":401,\"msg\":\"请先登录获取 Token\"}".getBytes());
            return false;
        }

        try {
            // 2. 使用 Sa-Token 校验 Token，无效或过期会抛出 NotLoginException
            Object loginId = StpUtil.getLoginIdByToken(token);

            if (loginId == null) {
                throw new NotLoginException("Token 不存在或已过期", token, null);
            }

            // 3. 把用户信息存到 attributes，Handler 中通过 session.getAttributes().get("loginId") 取用
            attributes.put("loginId", loginId);
            attributes.put("token", token);

            log.info("[WebSocket] 握手成功: 用户 {} 建立连接", loginId);
            return true;

        } catch (NotLoginException e) {
            // Token 无效或已过期
            log.warn("[WebSocket] 握手失败: Token 无效 - {}", e.getMessage());
            response.getBody().write("{\"code\":401,\"msg\":\"Token 无效或已过期\"}".getBytes());
            return false;
        }
    }

    /**
     * 握手后执行 - 可记录日志等
     */
    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        if (exception != null) {
            log.error("[WebSocket] 握手异常: {}", exception.getMessage());
        }
    }

    /**
     * 从请求中提取 Token，支持三种方式
     */
    private String getToken(ServerHttpRequest request) {
        // 方式1: URL 参数 ?satoken=xxx 或 ?token=xxx
        String query = request.getURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith(tokenName + "=")) {
                    return param.substring(tokenName.length() + 1);
                }
                if (param.startsWith("token=")) {
                    return param.substring(6);
                }
            }
        }

        // 方式2: Header satoken: xxx（与 sa-token 配置的 token-name 一致）
        String satoken = request.getHeaders().getFirst(tokenName);
        if (satoken != null && !satoken.isEmpty()) {
            return satoken;
        }

        // 方式3: Header Authorization: Bearer xxx
        String auth = request.getHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }

        return null;
    }
}
