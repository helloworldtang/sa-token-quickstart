package com.tangtang.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 *
 * @author tangtang
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    // 模拟数据
    private static final List<Map<String, Object>> USER_LIST = new ArrayList<>();

    static {
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", 1L);
        user1.put("username", "张三");
        user1.put("role", "admin");
        USER_LIST.add(user1);

        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", 2L);
        user2.put("username", "李四");
        user2.put("role", "user");
        USER_LIST.add(user2);
    }

    @Operation(summary = "获取用户列表")
    @SaCheckLogin
    @SaCheckPermission("user:list")
    @GetMapping("/list")
    public SaResult list() {
        return SaResult.ok().setData(USER_LIST);
    }

    @Operation(summary = "添加用户")
    @SaCheckLogin
    @SaCheckPermission("user:add")
    @PostMapping("/add")
    public SaResult add(@RequestParam String username) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", USER_LIST.size() + 1L);
        user.put("username", username);
        USER_LIST.add(user);
        return SaResult.ok("添加成功").setData(user);
    }

    @Operation(summary = "删除用户")
    @SaCheckLogin
    @SaCheckPermission("user:delete")
    @DeleteMapping("/delete/{id}")
    public SaResult delete(@PathVariable Long id) {
        USER_LIST.removeIf(user -> user.get("id").equals(id));
        return SaResult.ok("删除成功");
    }

    @Operation(summary = "获取当前用户信息")
    @SaCheckLogin
    @GetMapping("/profile")
    public SaResult profile() {
        Object userId = StpUtil.getLoginId();
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("message", "分布式 Session 验证成功！");
        return SaResult.ok().setData(data);
    }

    @Operation(summary = "管理员专属数据")
    @SaCheckLogin
    @SaCheckRole("admin")
    @GetMapping("/admin/data")
    public SaResult adminData() {
        return SaResult.ok("管理员专属数据");
    }
}
