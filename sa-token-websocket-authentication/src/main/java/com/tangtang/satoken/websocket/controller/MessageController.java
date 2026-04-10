package com.tangtang.satoken.websocket.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
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
 * 消息控制器
 *
 * 通过 HTTP 接口发送 WebSocket 消息
 *
 * @author 码骨丹心
 */
@Tag(name = "消息管理", description = "WebSocket 消息发送、在线用户查询")
@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private AuthWebSocketHandler webSocketHandler;

    /**
     * 发送广播消息
     */
    @Operation(summary = "发送广播消息", description = "向所有在线用户发送广播消息")
    @SaCheckLogin
    @PostMapping("/broadcast")
    public SaResult broadcast(
            @Parameter(description = "消息内容", required = true)
            @RequestParam String content) {

        Object senderId = cn.dev33.satoken.stp.StpUtil.getLoginId();
        webSocketHandler.broadcast(senderId, content);

        return SaResult.ok("广播发送成功");
    }

    /**
     * 发送私聊消息
     */
    @Operation(summary = "发送私聊消息", description = "向指定用户发送私聊消息")
    @SaCheckLogin
    @PostMapping("/private")
    public SaResult sendPrivate(
            @Parameter(description = "目标用户ID", required = true)
            @RequestParam Object targetId,
            @Parameter(description = "消息内容", required = true)
            @RequestParam String content) {

        Object senderId = cn.dev33.satoken.stp.StpUtil.getLoginId();
        webSocketHandler.sendPrivate(senderId, targetId, content);

        return SaResult.ok("私聊消息发送成功");
    }

    /**
     * 获取所有在线用户
     */
    @Operation(summary = "获取在线用户", description = "获取当前所有在线的用户列表")
    @SaCheckLogin
    @GetMapping("/online-users")
    public SaResult getOnlineUsers() {
        Set<Object> users = webSocketHandler.getOnlineUsers();

        Map<String, Object> data = new HashMap<>();
        data.put("count", users.size());
        data.put("users", users);

        return SaResult.ok().setData(data);
    }

    /**
     * 检查用户是否在线
     */
    @Operation(summary = "检查用户在线状态", description = "检查指定用户是否在线")
    @SaCheckLogin
    @GetMapping("/is-online/{userId}")
    public SaResult isOnline(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Object userId) {

        boolean online = webSocketHandler.isUserOnline(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("online", online);

        return SaResult.ok().setData(data);
    }

    /**
     * 获取服务状态
     */
    @Operation(summary = "获取服务状态", description = "获取 WebSocket 服务状态和统计信息")
    @GetMapping("/status")
    public SaResult getStatus() {
        Map<String, Object> data = new HashMap<>();
        data.put("onlineCount", webSocketHandler.getOnlineCount());
        data.put("websocketEnabled", true);
        data.put("wsEndpoint", "/ws");

        return SaResult.ok().setData(data);
    }
}
