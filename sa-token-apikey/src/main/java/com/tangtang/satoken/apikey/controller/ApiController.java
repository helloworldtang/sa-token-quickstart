package com.tangtang.satoken.apikey.controller;

import cn.dev33.satoken.apikey.template.SaApiKeyTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class ApiController {

    private final SaApiKeyTemplate saApiKeyTemplate;

    public ApiController(SaApiKeyTemplate saApiKeyTemplate) {
        this.saApiKeyTemplate = saApiKeyTemplate;
    }

    /** 聊天接口（模拟 LLM） */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> params,
                                    HttpServletRequest request) {
        String message = params.getOrDefault("message", "");
        Object loginId = request.getAttribute("apiKeyLoginId");  // ★ 从拦截器传来

        return Map.of(
                "code", 200,
                "message", "success",
                "data", Map.of(
                        "id", "chatcmpl-" + UUID.randomUUID().toString().substring(0, 8),
                        "message", Map.of(
                                "role", "assistant",
                                "content", "收到消息：「" + message + "」，来自 API Key [" + loginId + "]"
                        )
                )
        );
    }

    /** 模型列表 */
    @GetMapping("/models")
    public Map<String, Object> models(HttpServletRequest request) {
        Object loginId = request.getAttribute("apiKeyLoginId");
        return Map.of(
                "code", 200,
                "data", Map.of(
                        "object", "list",
                        "data", List.of(
                                Map.of("id", "gpt-4", "object", "model", "owned_by", "openai"),
                                Map.of("id", "gpt-3.5-turbo", "object", "model", "owned_by", "openai")
                        ),
                        "authenticated_with", loginId
                )
        );
    }

    /** 服务状态 */
    @GetMapping("/status")
    public Map<String, Object> status(HttpServletRequest request) {
        return Map.of(
                "code", 200,
                "data", Map.of(
                        "status", "ok",
                        "timestamp", new Date(),
                        "authenticated_with", request.getAttribute("apiKeyLoginId")
                )
        );
    }
}
