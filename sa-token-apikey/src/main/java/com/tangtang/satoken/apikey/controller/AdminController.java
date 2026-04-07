package com.tangtang.satoken.apikey.controller;

import cn.dev33.satoken.apikey.model.ApiKeyModel;
import cn.dev33.satoken.stp.StpUtil;
import com.tangtang.satoken.apikey.limiter.ApiKeyRateLimiter;
import com.tangtang.satoken.apikey.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Value("${app.admin-username:admin}")
    private String adminUsername;

    @Value("${app.admin-password:admin123}")
    private String adminPassword;

    @Resource
    private ApiKeyService apiKeyService;

    @Resource
    private ApiKeyRateLimiter apiKeyRateLimiter;

    // ---------- 认证 ----------

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        if (!adminUsername.equals(username) || !adminPassword.equals(password)) {
            return Map.of("code", 401, "message", "用户名或密码错误");
        }

        StpUtil.login(1001);  // ★ 登录，写入 Session
        return Map.of(
                "code", 200,
                "message", "登录成功",
                "data", Map.of(
                        "tokenValue", StpUtil.getTokenValue(),
                        "tokenName", StpUtil.getTokenName()
                )
        );
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        try {
            StpUtil.checkLogin();  // ★ 无 token 或 token 过期会抛异常
            return Map.of("code", 200, "data",
                    Map.of("loginId", StpUtil.getLoginId()));
        } catch (Exception e) {
            return Map.of("code", 401, "message", "未登录");
        }
    }

    @PostMapping("/logout")
    public Map<String, Object> logout() {
        StpUtil.logout();
        return Map.of("code", 200, "message", "已退出");
    }

    // ---------- API Key 管理 ----------

    @PostMapping("/apikey/create")
    public Map<String, Object> createApiKey(@RequestBody Map<String, String> params) {
        try {
            StpUtil.checkLogin();
            String name = params.getOrDefault("name", "未命名");
            ApiKeyModel ak = apiKeyService.createApiKey(StpUtil.getLoginId(), name);
            return Map.of(
                    "code", 200,
                    "message", "创建成功",
                    "data", Map.of(
                            "apiKey", ak.getApiKey(),         // ★ 只在这里返回完整 Key
                            "expiresTime", new Date(ak.getExpiresTime()),
                            "tip", "API Key 仅显示一次，请立即保存"
                    )
            );
        } catch (Exception e) {
            return Map.of("code", 401, "message", "未登录");
        }
    }

    @GetMapping("/apikey/list")
    public Map<String, Object> listApiKeys() {
        try {
            StpUtil.checkLogin();
            List<ApiKeyModel> list = apiKeyService.listApiKeys(StpUtil.getLoginId());
            List<Map<String, Object>> result = list.stream().map(ak -> {
                Map<String, Object> map = new HashMap<>();
                map.put("apiKey", ak.getApiKey());
                map.put("isValid", ak.getIsValid());
                map.put("createTime", new Date(ak.getCreateTime()));
                map.put("expiresTime", new Date(ak.getExpiresTime()));
                return map;
            }).collect(Collectors.toList());
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", result);
            return response;
        } catch (Exception e) {
            return Map.of("code", 401, "message", "未登录");
        }
    }

    @DeleteMapping("/apikey/{apiKey}")
    public Map<String, Object> deleteApiKey(@PathVariable String apiKey) {
        try {
            StpUtil.checkLogin();
            apiKeyService.deleteApiKey(StpUtil.getLoginId(), apiKey);
            return Map.of("code", 200, "message", "已删除");
        } catch (Exception e) {
            return Map.of("code", 401, "message", "未登录");
        }
    }

    // ---------- 限流统计 ----------

    @GetMapping("/apikey/{apiKey}/stats")
    public Map<String, Object> getStats(@PathVariable String apiKey) {
        try {
            StpUtil.checkLogin();
            return Map.of("code", 200,
                    "data", apiKeyRateLimiter.getUsageStats(apiKey));
        } catch (Exception e) {
            return Map.of("code", 401, "message", "未登录");
        }
    }

    @PostMapping("/apikey/{apiKey}/reset-rate-limit")
    public Map<String, Object> resetRateLimit(@PathVariable String apiKey) {
        try {
            StpUtil.checkLogin();
            apiKeyRateLimiter.resetRateLimit(apiKey);
            return Map.of("code", 200, "message", "已重置分钟限流");
        } catch (Exception e) {
            return Map.of("code", 401, "message", "未登录");
        }
    }
}
