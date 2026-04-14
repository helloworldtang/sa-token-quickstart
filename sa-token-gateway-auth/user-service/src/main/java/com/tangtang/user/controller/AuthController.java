package com.tangtang.user.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 *
 * @author tangtang
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public SaResult login(@RequestParam String username, @RequestParam String password) {
        // 模拟校验
        if ("admin".equals(username) && "123456".equals(password)) {
            // 登录
            StpUtil.login(10001);
            Map<String, Object> data = new HashMap<>();
            data.put("userId", 10001);
            data.put("username", username);
            data.put("token", StpUtil.getTokenValue());
            return SaResult.ok("登录成功").setData(data);
        } else if ("user".equals(username) && "123456".equals(password)) {
            // 登录
            StpUtil.login(10002);
            Map<String, Object> data = new HashMap<>();
            data.put("userId", 10002);
            data.put("username", username);
            data.put("token", StpUtil.getTokenValue());
            return SaResult.ok("登录成功").setData(data);
        }
        return SaResult.error("用户名或密码错误");
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/info")
    public SaResult getInfo() {
        Object userId = StpUtil.getLoginId();
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("token", StpUtil.getTokenValue());
        return SaResult.ok().setData(data);
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public SaResult logout() {
        StpUtil.logout();
        return SaResult.ok("退出成功");
    }
}
