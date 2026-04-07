package com.tangtang.satoken.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录认证控制器
 * 
 * 演示 Sa-Token 核心 API：
 * - 登录: StpUtil.login(userId)
 * - 登出: StpUtil.logout()
 * - 检查登录: StpUtil.isLogin()
 * - 获取当前用户: StpUtil.getLoginId()
 * 
 * 模拟用户：
 * - admin / 123456 → 用户ID: 10001，拥有 admin 角色和所有权限
 * - user / 123456 → 用户ID: 10002，拥有 user 角色和 user:list 权限
 * 
 * @author 码骨丹心
 */
@Tag(name = "认证管理", description = "登录、登出、登录状态检查")
@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * 登录接口
     * 
     * 一行代码搞定登录！
     * StpUtil.login(userId) 会自动：
     * 1. 生成 Token
     * 2. 写入 Cookie/Header
     * 3. 记录登录态到 Session
     * 
     * ⚠️ 权限说明：
     * 权限数据在 StpInterfaceImpl 中根据用户ID获取
     * 实际项目应从数据库查询用户的角色和权限
     */
    @Operation(summary = "用户登录", description = "用户名密码登录，返回 token")
    @PostMapping("/login")
    public SaResult login(
            @Parameter(description = "用户名 (admin 或 user)", required = true, example = "admin")
            @RequestParam String username,
            @Parameter(description = "密码 (123456)", required = true, example = "123456")
            @RequestParam String password) {
        // 模拟登录校验（实际应从数据库查询）
        Long userId = null;
        
        if ("admin".equals(username) && "123456".equals(password)) {
            userId = 10001L;  // admin 用户
        } else if ("user".equals(username) && "123456".equals(password)) {
            userId = 10002L;  // 普通用户
        }
        
        if (userId != null) {
            // 🔥 核心：一行代码完成登录！
            StpUtil.login(userId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", StpUtil.getTokenValue());
            data.put("userId", StpUtil.getLoginId());
            
            return SaResult.ok("登录成功").setData(data);
        }
        return SaResult.error("用户名或密码错误");
    }

    /**
     * 登出接口
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
     * 获取当前登录用户信息
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/info")
    public SaResult info() {
        // 检查是否登录
        StpUtil.checkLogin();
        
        Map<String, Object> data = new HashMap<>();
        data.put("userId", StpUtil.getLoginId());
        data.put("token", StpUtil.getTokenValue());
        data.put("sessionTimeout", StpUtil.getTokenTimeout());
        
        return SaResult.ok().setData(data);
    }
}
