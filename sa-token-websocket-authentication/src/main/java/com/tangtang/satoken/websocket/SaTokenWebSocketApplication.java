package com.tangtang.satoken.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Sa-Token + WebSocket 鉴权示例启动类
 *
 * 核心功能：
 * - HTTP 登录认证（使用 Sa-Token）
 * - WebSocket 连接鉴权（首次连接需要携带 Token）
 * - 消息广播（已认证用户可发送消息）
 * - 在线用户管理
 * - 心跳检测和连接清理
 *
 * @author 码骨丹心
 */
@SpringBootApplication
@EnableScheduling
public class SaTokenWebSocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaTokenWebSocketApplication.class, args);
        System.out.println("✅ Sa-Token WebSocket 鉴权示例启动成功！");
        System.out.println("📖 API 文档: http://localhost:8084/doc.html");
        System.out.println("🔌 WebSocket: ws://localhost:8084/ws");
        System.out.println();
        System.out.println("💡 快速测试：");
        System.out.println("   1. POST http://localhost:8084/auth/login?username=admin&password=123456 获取 Token");
        System.out.println("   2. 使用 Token 连接 WebSocket 进行认证");
    }
}
