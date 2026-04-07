package com.tangtang.satoken.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限控制控制器
 * 
 * 演示 Sa-Token 权限注解：
 * - @SaCheckLogin: 登录校验
 * - @SaCheckRole: 角色校验
 * - @SaCheckPermission: 权限校验
 * 
 * @author 码骨丹心
 */
@Tag(name = "用户管理", description = "用户信息、权限控制示例")
@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 获取用户信息 - 需要登录
     */
    @Operation(summary = "获取用户信息", description = "需要登录，返回当前用户ID")
    @SaCheckLogin
    @GetMapping("/profile")
    public SaResult profile() {
        return SaResult.ok("当前用户ID: " + StpUtil.getLoginId());
    }

    /**
     * 管理员接口 - 需要 admin 角色
     */
    @Operation(summary = "管理员数据", description = "需要 admin 角色")
    @SaCheckRole("admin")
    @GetMapping("/admin/data")
    public SaResult adminData() {
        return SaResult.ok("管理员专属数据");
    }

    /**
     * 用户列表 - 需要 user:list 权限
     */
    @Operation(summary = "用户列表", description = "需要 user:list 权限")
    @SaCheckPermission("user:list")
    @GetMapping("/list")
    public SaResult list() {
        List<String> users = Arrays.asList("张三", "李四", "王五");
        return SaResult.ok().setData(users);
    }

    /**
     * 添加用户 - 需要 user:add 权限
     */
    @Operation(summary = "添加用户", description = "需要 user:add 权限")
    @SaCheckPermission("user:add")
    @PostMapping("/add")
    public SaResult add(
            @Parameter(description = "用户名", required = true, example = "新用户")
            @RequestParam String username) {
        return SaResult.ok("添加用户成功: " + username);
    }

    /**
     * 删除用户 - 需要 user:delete 权限
     */
    @Operation(summary = "删除用户", description = "需要 user:delete 权限")
    @SaCheckPermission("user:delete")
    @PostMapping("/delete")
    public SaResult delete(
            @Parameter(description = "用户ID", required = true, example = "1")
            @RequestParam Long userId) {
        return SaResult.ok("删除用户成功: " + userId);
    }

    /**
     * 获取当前用户权限列表
     */
    @Operation(summary = "获取权限列表", description = "获取当前用户的角色和权限列表")
    @SaCheckLogin
    @GetMapping("/permissions")
    public SaResult permissions() {
        // 从 Sa-Token 获取当前用户的权限列表
        List<String> permissions = StpUtil.getPermissionList();
        List<String> roles = StpUtil.getRoleList();
        
        Map<String, Object> data = new HashMap<>();
        data.put("permissions", permissions);
        data.put("roles", roles);
        
        return SaResult.ok().setData(data);
    }
}
