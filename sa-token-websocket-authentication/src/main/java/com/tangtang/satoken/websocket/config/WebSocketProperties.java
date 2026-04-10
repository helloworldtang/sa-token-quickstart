package com.tangtang.satoken.websocket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * WebSocket 配置属性
 *
 * @author 码骨丹心
 */
@Component
@ConfigurationProperties(prefix = "websocket")
public class WebSocketProperties {

    /**
     * 是否启用 WebSocket
     */
    private boolean enabled = true;

    /**
     * WebSocket 路径
     */
    private String path = "/ws";

    /**
     * 认证超时时间（秒）
     */
    private int authTimeout = 30;

    /**
     * 心跳间隔（秒）
     */
    private int heartBeatInterval = 30;

    /**
     * 连接超时时间（秒）
     */
    private int connectionTimeout = 120;

    /**
     * 最大连接数
     */
    private int maxConnections = 10000;

    /**
     * 广播超时时间（毫秒）
     */
    private int broadcastTimeout = 5000;

    /**
     * 是否启用异步消息发送
     */
    private boolean asyncEnabled = true;

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getAuthTimeout() {
        return authTimeout;
    }

    public void setAuthTimeout(int authTimeout) {
        this.authTimeout = authTimeout;
    }

    public int getHeartBeatInterval() {
        return heartBeatInterval;
    }

    public void setHeartBeatInterval(int heartBeatInterval) {
        this.heartBeatInterval = heartBeatInterval;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getBroadcastTimeout() {
        return broadcastTimeout;
    }

    public void setBroadcastTimeout(int broadcastTimeout) {
        this.broadcastTimeout = broadcastTimeout;
    }

    public boolean isAsyncEnabled() {
        return asyncEnabled;
    }

    public void setAsyncEnabled(boolean asyncEnabled) {
        this.asyncEnabled = asyncEnabled;
    }
}
