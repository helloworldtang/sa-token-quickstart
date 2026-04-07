package com.tangtang.satoken.apikey.interceptor;

import cn.dev33.satoken.apikey.template.SaApiKeyTemplate;
import com.tangtang.satoken.apikey.limiter.ApiKeyRateLimiter;
import com.tangtang.satoken.apikey.limiter.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    private final SaApiKeyTemplate apikeyTemplate;
    private final RateLimiter rateLimiter;
    private final ApiKeyRateLimiter apiKeyRateLimiter;

    public ApiKeyInterceptor(SaApiKeyTemplate apikeyTemplate, RateLimiter rateLimiter, ApiKeyRateLimiter apiKeyRateLimiter) {
        this.apikeyTemplate = apikeyTemplate;
        this.rateLimiter = rateLimiter;
        this.apiKeyRateLimiter = apiKeyRateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler) throws Exception {

        // ---------- ① IP 限流 ----------
        String clientIp = getClientIp(request);
        if (!rateLimiter.allowRequest(clientIp)) {
            return error(response, 429, "IP 请求过于频繁");
        }

        // 设置限流响应头，客户端可感知剩余配额
        response.setHeader("X-RateLimit-Limit",
                String.valueOf(rateLimiter.getMaxRequests()));
        response.setHeader("X-RateLimit-Remaining",
                String.valueOf(rateLimiter.getRemaining(clientIp)));

        // ---------- ② 提取 API Key ----------
        String apiKey = extractApiKey(request);
        if (apiKey == null || apiKey.isEmpty()) {
            return error(response, 401, "缺少 API Key，Header 格式：Authorization: sk-xxx");
        }

        // ---------- ③ 验证 API Key ----------
        try {
            apikeyTemplate.checkApiKey(apiKey);  // 内部处理过期 / 禁用检查
        } catch (Exception e) {
            return error(response, 401, "API Key 无效或已过期");
        }

        // ---------- ④ API Key 限流（分钟级） ----------
        if (!apiKeyRateLimiter.allowRequest(apiKey)) {
            return error(response, 429, "API Key 每分钟请求次数超限");
        }

        // ---------- ⑤ API Key 每日限流 ----------
        if (!apiKeyRateLimiter.allowDailyRequest(apiKey)) {
            return error(response, 429, "API Key 每日请求次数超限");
        }

        // ---------- ⑥ 设置请求属性，给 Controller 用 ----------
        request.setAttribute("apiKeyLoginId", apikeyTemplate.getLoginIdByApiKey(apiKey));
        return true;
    }

    // ★ 从请求中提取 API Key（支持三种方式）
    private String extractApiKey(HttpServletRequest request) {
        // 方式1：?apikey=sk-xxx（GET 参数）
        String apiKey = request.getParameter("apikey");
        if (apiKey != null && !apiKey.isEmpty()) return apiKey;

        // 方式2：Authorization: Bearer sk-xxx 或 Authorization: sk-xxx
        String auth = request.getHeader("Authorization");
        if (auth != null && !auth.isEmpty()) {
            if (auth.startsWith("Bearer ")) {
                return auth.substring(7);  // 去掉 "Bearer " 前缀
            }
            return auth;  // 直接使用 Authorization 的值（sk-xxx 格式）
        }

        // 方式3：apikey: sk-xxx（自定义 Header）
        apiKey = request.getHeader("apikey");
        if (apiKey != null && !apiKey.isEmpty()) return apiKey;

        return null;
    }

    // ★ 获取真实 IP（代理环境下也要能拿到）
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private boolean error(HttpServletResponse response, int status, String msg) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + status + ",\"msg\":\"" + msg + "\"}");
        return false;
    }
}
