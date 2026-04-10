package com.tangtang.satoken.websocket.handler;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 消息处理器
 *
 * 核心功能：
 * 1. 管理所有 WebSocket 连接会话
 * 2. 处理消息的发送和广播
 * 3. 记录在线用户
 *
 * @author 码骨丹心
 */
@Component
public class AuthWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthWebSocketHandler.class);

    /**
     * 在线会话存储
     * key: loginId (用户ID)
     * value: WebSocketSession
     *
     * 实际生产中可能需要分布式存储（如 Redis）
     */
    private final Map<Object, WebSocketSession> onlineSessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== 生命周期回调 ====================

    /**
     * 连接建立成功后调用
     * loginId 已通过拦截器校验并存储在 session 属性中
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Object loginId = session.getAttributes().get("loginId");
        if (loginId != null) {
            onlineSessions.put(loginId, session);
            log.info("[WebSocket] 用户 {} 已连接，当前在线: {} 人", loginId, onlineSessions.size());

            // 发送欢迎消息
            sendMessage(session, buildMessage("system", "系统消息",
                    "欢迎回来！您已成功连接到 WebSocket 服务器", loginId));
        }
    }

    /**
     * 收到文本消息时调用
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Object loginId = session.getAttributes().get("loginId");
        if (loginId == null) {
            sendMessage(session, buildMessage("error", "错误", "未认证的连接", null));
            return;
        }

        String payload = message.getPayload();
        log.debug("[WebSocket] 收到用户 {} 的消息: {}", loginId, payload);

        try {
            // 解析消息
            Map<String, Object> msgData = objectMapper.readValue(payload, Map.class);
            String type = (String) msgData.getOrDefault("type", "unknown");

            switch (type) {
                case "ping":
                    // 心跳检测
                    sendMessage(session, buildMessage("pong", "心跳", "pong", loginId));
                    break;

                case "broadcast":
                    // 广播消息
                    String content = (String) msgData.get("content");
                    if (content != null && !content.isEmpty()) {
                        broadcast(loginId, content);
                    }
                    break;

                case "private":
                    // 私聊消息
                    Object targetId = msgData.get("targetId");
                    String privateContent = (String) msgData.get("content");
                    if (targetId != null && privateContent != null) {
                        sendPrivate(loginId, targetId, privateContent);
                    }
                    break;

                default:
                    sendMessage(session, buildMessage("error", "未知消息类型", "支持: ping, broadcast, private", loginId));
            }

        } catch (Exception e) {
            log.error("[WebSocket] 消息处理异常: {}", e.getMessage());
            sendMessage(session, buildMessage("error", "消息解析失败", e.getMessage(), loginId));
        }
    }

    /**
     * 连接关闭时调用
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Object loginId = session.getAttributes().get("loginId");
        if (loginId != null) {
            onlineSessions.remove(loginId);
            log.info("[WebSocket] 用户 {} 已断开连接 ({}), 当前在线: {} 人",
                    loginId, status, onlineSessions.size());
        }
    }

    /**
     * 传输错误时调用
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Object loginId = session.getAttributes().get("loginId");
        log.error("[WebSocket] 用户 {} 传输错误: {}", loginId, exception.getMessage());
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }

    // ==================== 消息发送方法 ====================

    /**
     * 发送消息给指定会话
     */
    public void sendMessage(WebSocketSession session, Map<String, Object> message) {
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                log.error("[WebSocket] 发送消息失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 发送消息给指定用户
     */
    public void sendToUser(Object loginId, Map<String, Object> message) {
        WebSocketSession session = onlineSessions.get(loginId);
        sendMessage(session, message);
    }

    /**
     * 广播消息给所有在线用户
     */
    public void broadcast(Object senderId, String content) {
        Map<String, Object> message = buildMessage("broadcast", "广播消息",
                "[" + senderId + "]: " + content, senderId);

        for (Map.Entry<Object, WebSocketSession> entry : onlineSessions.entrySet()) {
            sendMessage(entry.getValue(), message);
        }

        log.info("[WebSocket] 用户 {} 发送广播: {}", senderId, content);
    }

    /**
     * 发送私聊消息
     */
    public void sendPrivate(Object senderId, Object targetId, String content) {
        Map<String, Object> message = buildMessage("private", "私聊消息",
                content, senderId);

        // 发送给目标用户
        WebSocketSession targetSession = onlineSessions.get(targetId);
        if (targetSession != null) {
            sendMessage(targetSession, message);
            // 同时通知发送者消息已送达
            sendMessage(onlineSessions.get(senderId),
                    buildMessage("private", "私聊消息(发送成功)",
                            "To [" + targetId + "]: " + content, senderId));
        } else {
            // 目标用户不在线
            sendMessage(onlineSessions.get(senderId),
                    buildMessage("error", "发送失败", "用户 " + targetId + " 不在线", senderId));
        }

        log.info("[WebSocket] 用户 {} 向 {} 发送私聊: {}", senderId, targetId, content);
    }

    // ==================== 工具方法 ====================

    /**
     * 构建统一消息格式
     */
    private Map<String, Object> buildMessage(String type, String title, String content, Object from) {
        return Map.of(
                "type", type,
                "title", title,
                "content", content,
                "from", from != null ? from.toString() : "system",
                "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * 获取所有在线用户
     */
    public Set<Object> getOnlineUsers() {
        return onlineSessions.keySet();
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineCount() {
        return onlineSessions.size();
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Object loginId) {
        return onlineSessions.containsKey(loginId);
    }
}
