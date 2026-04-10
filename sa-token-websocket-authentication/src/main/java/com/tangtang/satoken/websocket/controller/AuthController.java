package com.tangtang.satoken.websocket.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.tangtang.satoken.websocket.handler.AuthWebSocketHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 认证控制器
 *
 * @author 码骨丹心
 */
@Tag(name = "认证管理", description = "登录、登出、登录状态检查")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthWebSocketHandler webSocketHandler;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "使用用户名密码登录，返回 Token")
    @PostMapping("/login")
    public SaResult login(
            @Parameter(description = "用户名", required = true, example = "admin")
            @RequestParam String username,
            @Parameter(description = "密码", required = true, example = "123456")
            @RequestParam String password) {

        // 模拟登录校验
        Long userId = null;
        if ("admin".equals(username) && "123456".equals(password)) {
            userId = 10001L;
        } else if ("user".equals(username) && "123456".equals(password)) {
            userId = 10002L;
        } else if ("test".equals(username) && "123456".equals(password)) {
            userId = 10003L;
        }

        if (userId != null) {
            // 🔥 核心：一行代码完成登录
            StpUtil.login(userId);

            Map<String, Object> data = new HashMap<>();
            data.put("token", StpUtil.getTokenValue());
            data.put("userId", StpUtil.getLoginId());

            return SaResult.ok("登录成功").setData(data);
        }

        return SaResult.error("用户名或密码错误");
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "退出登录状态")
    @PostMapping("/logout")
    public SaResult logout() {
        StpUtil.logout();
        return SaResult.ok("登出成功");
    }

    /**
     * 检查登录状态
     */
    @Operation(summary = "检查登录状态", description = "检查当前用户是否已登录")
    @GetMapping("/isLogin")
    public SaResult isLogin() {
        boolean login = StpUtil.isLogin();
        return SaResult.ok().setData(login);
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/info")
    public SaResult info() {
        StpUtil.checkLogin();

        Map<String, Object> data = new HashMap<>();
        data.put("userId", StpUtil.getLoginId());
        data.put("token", StpUtil.getTokenValue());
        data.put("sessionTimeout", StpUtil.getTokenTimeout());

        return SaResult.ok().setData(data);
    }

    /**
     * 获取 WebSocket 连接地址
     */
    @Operation(summary = "获取 WebSocket 地址", description = "获取带 Token 的 WebSocket 连接地址")
    @GetMapping("/ws-url")
    public SaResult getWebSocketUrl() {
        StpUtil.checkLogin();

        String token = StpUtil.getTokenValue();
        String wsUrl = "ws://localhost:8084/ws?token=" + token;

        Map<String, Object> data = new HashMap<>();
        data.put("wsUrl", wsUrl);
        data.put("token", token);
        data.put("tip", "将此地址粘贴到浏览器控制台或 WebSocket 测试工具中连接");

        return SaResult.ok().setData(data);
    }
}
