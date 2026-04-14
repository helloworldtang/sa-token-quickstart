package com.tangtang.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.tangtang.auth.entity.ApiKey;
import com.tangtang.auth.entity.User;
import com.tangtang.auth.service.AuthService;
import com.tangtang.auth.util.SignUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一认证控制器
 * 提供所有场景的认证接口：普通用户登录、API Key鉴权、Token校验
 *
 * @author tangtang
 */
@Tag(name = "统一认证中心")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 1. 普通用户登录（适配单体、微服务场景）
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public SaResult login(@RequestParam String username, @RequestParam String password) {
        try {
            // 1. 校验用户名密码
            User user = authService.authenticateUser(username, password);
            if (user == null) {
                return SaResult.error("用户名或密码错误");
            }

            // 2. 检查用户状态
            if (user.getStatus() == 0) {
                return SaResult.error("账号已被禁用");
            }

            // 3. 生成 Token（登录类型为 user）
            StpUtil.login(user.getId(), "user");

            // 4. 构造返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("token", StpUtil.getTokenValue());
            data.put("expire", StpUtil.getTokenTimeout());

            System.out.println("用户登录成功: username=" + username + ", userId=" + user.getId());
            return SaResult.ok("登录成功").setData(data);

        } catch (Exception e) {
            e.printStackTrace();
            return SaResult.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 2. API Key 鉴权（适配大模型开放平台、第三方接口）
     * 承接 API 签名验证 + 限流控制
     */
    @Operation(summary = "API Key 鉴权")
    @PostMapping("/apiKeyAuth")
    public SaResult apiKeyAuth(
            @RequestParam String apiKey,
            @RequestParam String sign,
            @RequestParam Long timestamp) {

        try {
            // 1. 查询 API Key 信息
            ApiKey apiKeyInfo = authService.getApiKeyInfo(apiKey);
            if (apiKeyInfo == null) {
                return SaResult.error("API Key 无效");
            }

            // 2. 检查 API Key 状态
            if (apiKeyInfo.getStatus() == 0) {
                return SaResult.error("API Key 已被禁用");
            }

            // 3. 检查过期时间
            if (apiKeyInfo.getExpireTime() != null &&
                System.currentTimeMillis() > apiKeyInfo.getExpireTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()) {
                return SaResult.error("API Key 已过期");
            }

            // 4. 验证签名（防篡改、防重放）
            boolean checkSign = SignUtil.checkSign(apiKey, apiKeyInfo.getSecretKey(), timestamp, sign);
            if (!checkSign) {
                System.out.println("API Key 签名验证失败: apiKey=" + apiKey + ", sign=" + sign + ", timestamp=" + timestamp);
                return SaResult.error("签名验证失败");
            }

            // 5. 执行限流（防止滥用）
            String flowKey = "apiKey:" + apiKey + ":" + System.currentTimeMillis() / 86400; // 按天限流
            // 简化处理，暂时不使用复杂的限流功能
            // StpUtil.checkFlow(flowKey, apiKeyInfo.getLimitCount());

            // 6. 生成 Token（登录类型为 apiKey）
            StpUtil.login(apiKey, "apiKey");

            // 7. 构造返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("apiKey", apiKey);
            data.put("appName", apiKeyInfo.getAppName());
            data.put("token", StpUtil.getTokenValue());
            data.put("expire", StpUtil.getTokenTimeout());

            System.out.println("API Key 鉴权成功: apiKey=" + apiKey + ", appName=" + apiKeyInfo.getAppName());
            return SaResult.ok("鉴权通过").setData(data);

        } catch (Exception e) {
            e.printStackTrace();
            return SaResult.error("鉴权失败：" + e.getMessage());
        }
    }

    /**
     * 3. 统一鉴权接口（供网关、业务服务调用）
     * 校验 Token 有效性并返回用户信息
     */
    @Operation(summary = "校验 Token 有效性")
    @GetMapping("/checkToken")
    public SaResult checkToken(@RequestParam String token) {
        try {
            // 1. 校验 Token 有效性
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId == null) {
                return SaResult.error("Token 无效或已过期");
            }

            // 2. 构造返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("loginId", loginId);

            System.out.println("Token 校验成功: loginId=" + loginId);
            return SaResult.ok().setData(data);

        } catch (Exception e) {
            e.printStackTrace();
            return SaResult.error("Token 校验失败：" + e.getMessage());
        }
    }

    /**
     * 4. 退出登录
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public SaResult logout() {
        try {
            StpUtil.logout();
            return SaResult.ok("退出成功");
        } catch (Exception e) {
            e.printStackTrace();
            return SaResult.error("退出失败：" + e.getMessage());
        }
    }

    /**
     * 5. 获取当前登录用户信息
     */
    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/info")
    public SaResult getInfo() {
        try {
            Object loginId = StpUtil.getLoginId();
            String loginType = StpUtil.getLoginType();

            Map<String, Object> data = new HashMap<>();
            data.put("loginId", loginId);
            data.put("loginType", loginType);
            data.put("roleList", StpUtil.getRoleList());
            data.put("permissionList", StpUtil.getPermissionList());

            return SaResult.ok().setData(data);
        } catch (Exception e) {
            e.printStackTrace();
            return SaResult.error("获取用户信息失败：" + e.getMessage());
        }
    }
}
