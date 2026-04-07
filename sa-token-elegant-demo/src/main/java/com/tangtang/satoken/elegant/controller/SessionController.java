package com.tangtang.satoken.elegant.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.tangtang.satoken.common.model.Result;
import com.tangtang.satoken.elegant.model.dto.KickoutRequest;
import com.tangtang.satoken.elegant.model.dto.OnlineSessionDTO;
import com.tangtang.satoken.elegant.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话管理控制器
 * <p>
 * 优雅点展示：
 * 1. 提供在线会话管理功能
 * 2. 支持根据用户ID和会话ID踢人
 * 3. 注解驱动的权限控制
 * 4. 完整的Swagger文档
 *
 * @author Agent唐
 */
@RestController
@RequestMapping("/admin")
@Tag(name = "会话管理", description = "用户会话管理接口")
public class SessionController {

    private static final Logger log = LoggerFactory.getLogger(SessionController.class);
    private final AuthService authService;

    public SessionController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 获取在线会话列表
     * <p>
     * 优雅点展示：
     * 1. 返回在线用户的会话信息
     * 2. 不包含敏感Token
     * 3. 支持会话管理
     */
    @GetMapping("/online-sessions")
    @SaCheckLogin
    @SaCheckRole("admin")
    @Operation(summary = "获取在线会话列表", description = "查看所有在线用户的会话信息（需要admin角色）")
    public Result<List<OnlineSessionDTO>> getOnlineSessions() {
        log.info("管理员查看在线会话列表");
        List<OnlineSessionDTO> sessions = authService.getOnlineSessions();
        return Result.success(sessions);
    }

    /**
     * 根据用户ID踢人
     * <p>
     * 优雅点展示：
     * 1. 根据用户ID踢掉所有会话
     * 2. 符合后台管理习惯
     * 3. 无需知道用户Token
     * 4. 支持踢人原因记录
     */
    @PostMapping("/kickout/{userId}")
    @SaCheckLogin
    @SaCheckRole("admin")
    @Operation(summary = "踢人下线", description = "根据用户ID踢掉用户所有会话（需要admin角色）")
    public Result<Void> kickoutByUserId(
            @PathVariable Long userId,
            @Valid @RequestBody(required = false) KickoutRequest request
    ) {
        log.info("管理员踢人: userId={}", userId);
        authService.kickoutByUserId(userId, request);
        return Result.success("踢人成功");
    }

    /**
     * 踢掉指定会话
     * <p>
     * 优雅点展示：
     * 1. 根据会话ID踢掉单个会话
     * 2. 精细化控制
     * 3. 支持多设备登录场景
     * 4. 支持踢人原因记录
     */
    @PostMapping("/kickout-session/{sessionId}")
    @SaCheckLogin
    @SaCheckRole("admin")
    @Operation(summary = "踢掉指定会话", description = "根据会话ID踢掉指定会话（需要admin角色）")
    public Result<Void> kickoutBySessionId(
            @PathVariable String sessionId,
            @Valid @RequestBody(required = false) KickoutRequest request
    ) {
        log.info("管理员踢掉会话: sessionId={}", sessionId);
        authService.kickoutBySessionId(sessionId, request);
        return Result.success("踢掉会话成功");
    }
}