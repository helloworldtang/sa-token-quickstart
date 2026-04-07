package com.tangtang.satoken.elegant.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.tangtang.satoken.common.model.Result;
import com.tangtang.satoken.elegant.model.dto.LoginRequest;
import com.tangtang.satoken.elegant.model.dto.LoginResponse;
import com.tangtang.satoken.elegant.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * <p>
 * 优雅点展示：
 * 1. 使用Sa-Token注解，声明式权限控制
 * 2. 注解语义清晰，易于理解
 * 3. 无需手动编写拦截器或过滤器
 * 4. 代码更简洁，可读性更强
 *
 * @author Agent唐
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "用户登录、登出、踢人等接口")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录
     * <p>
     * 优雅点：
     * - 无需任何注解，登录接口不需要认证
     * - 使用 @Valid 自动校验参数
     * - Swagger注解自动生成文档
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录，返回Token")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    /**
     * 用户登出
     * <p>
     * 优雅点：
     * - @SaCheckLogin 注解，要求必须登录
     * - Sa-Token自动校验，未登录自动返回401
     * - 无需手动编写校验逻辑
     */
    @PostMapping("/logout")
    @SaCheckLogin
    @Operation(summary = "用户登出", description = "退出登录，清除Token")
    public Result<Void> logout() {
        authService.logout();
        return Result.success("登出成功");
    }

    /**
     * 获取当前用户信息
     * <p>
     * 优雅点：
     * - @SaCheckLogin 注解，要求必须登录
     * - 自动校验登录状态
     */
    @GetMapping("/current")
    @SaCheckLogin
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public Result<LoginResponse> getCurrentUser() {
        LoginResponse response = authService.getCurrentUser();
        return Result.success(response);
    }

}
