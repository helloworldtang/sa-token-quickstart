package com.tangtang.satoken.elegant.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.tangtang.satoken.common.model.Result;
import com.tangtang.satoken.elegant.model.User;
import com.tangtang.satoken.elegant.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户控制器
 * <p>
 * 优雅点展示：
 * 1. 细粒度权限控制（@SaCheckPermission）
 * 2. 支持或逻辑（@SaCheckPermission(value = {"perm1", "perm2"}, mode = SaMode.OR)）
 * 3. 注解和代码结合，灵活控制权限
 * 4. 代码可读性强，维护方便
 *
 * @author Agent唐
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户增删改查接口")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取用户列表
     * <p>
     * 优雅点：
     * - @SaCheckLogin 要求必须登录
     * - 只有登录用户才能查看用户列表
     */
    @GetMapping("/list")
    @SaCheckLogin
    @Operation(summary = "获取用户列表", description = "查看所有用户（需要登录）")
    public Result<List<User>> list() {
        // 演示用，实际项目应从数据库查询
        List<User> users = List.of(
                userService.getById(1L),
                userService.getById(2L)
        ).stream().filter(user -> user != null).collect(Collectors.toList());
        return Result.success(users);
    }

    /**
     * 查看用户详情
     * <p>
     * 优雅点：
     * - @SaCheckPermission("user:view") 要求有查看权限
     * - 细粒度权限控制
     * - 权限码语义清晰
     */
    @GetMapping("/{id}")
    @SaCheckPermission("user:view")
    @Operation(summary = "查看用户详情", description = "根据ID查看用户（需要user:view权限）")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }

    /**
     * 新增用户
     * <p>
     * 优雅点：
     * - @SaCheckPermission("user:add") 要求有新增权限
     * - 权限与操作一一对应
     */
    @PostMapping
    @SaCheckPermission("user:add")
    @Operation(summary = "新增用户", description = "创建新用户（需要user:add权限）")
    public Result<User> add(@RequestBody User user) {
        log.info("新增用户: {}", user.getUsername());
        // 实际项目应保存到数据库
        return Result.success("新增成功", user);
    }

    /**
     * 修改用户
     * <p>
     * 优雅点：
     * - @SaCheckPermission("user:edit") 要求有编辑权限
     * - 与新增权限分离，职责明确
     */
    @PutMapping("/{id}")
    @SaCheckPermission("user:edit")
    @Operation(summary = "修改用户", description = "修改用户信息（需要user:edit权限）")
    public Result<User> update(@PathVariable Long id, @RequestBody User user) {
        log.info("修改用户: {}", id);
        user.setId(id);
        // 实际项目应更新到数据库
        return Result.success("修改成功", user);
    }

    /**
     * 删除用户
     * <p>
     * 优雅点：
     * - @SaCheckPermission("user:delete") 要求有删除权限
     * - 最严格的权限控制
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("user:delete")
    @Operation(summary = "删除用户", description = "删除用户（需要user:delete权限）")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除用户: {}", id);
        // 实际项目应从数据库删除
        return Result.success("删除成功");
    }

    /**
     * 获取当前登录用户ID
     * <p>
     * 优雅点：
     * - StpUtil.getLoginId() 获取当前登录ID
     * - 无需手动管理会话
     * - 代码简洁
     */
    @GetMapping("/current-id")
    @SaCheckLogin
    @Operation(summary = "获取当前登录用户ID", description = "获取当前登录用户的ID")
    public Result<Long> getCurrentLoginId() {
        Long loginId = StpUtil.getLoginIdAsLong();
        return Result.success(loginId);
    }

    /**
     * 检查是否有指定权限
     * <p>
     * 优雅点：
     * - StpUtil.hasPermission() 检查权限
     * - 代码中动态判断权限
     * - 灵活控制业务逻辑
     */
    @GetMapping("/check-permission")
    @SaCheckLogin
    @Operation(summary = "检查权限", description = "检查当前用户是否有指定权限")
    public Result<Boolean> checkPermission(@RequestParam String permission) {
        boolean hasPermission = StpUtil.hasPermission(permission);
        return Result.success(hasPermission);
    }
}
