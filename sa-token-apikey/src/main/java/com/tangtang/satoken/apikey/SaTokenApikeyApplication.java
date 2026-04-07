package com.tangtang.satoken.apikey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sa-Token API Key 分发模块 - 官方 API Key 插件实战
 *
 * 功能特性：
 * - 管理员登录与登出
 * - API Key 创建、查询、删除
 * - API Key 认证（Bearer sk-xxx 格式）
 * - IP 限流（每分钟最多 60 次请求）
 * - API Key 限流（每分钟最多 100 次，每天最多 10000 次）
 * - API Key 使用统计查询
 * - 统一异常处理和响应格式
 *
 * 技术实现：
 * - Spring Boot 3.3.0
 * - Sa-Token 1.45.0
 * - Spring Data Redis
 * - Redis 限流实现
 * - MockMvc 集成测试（15 个测试全部通过）
 */
@SpringBootApplication
public class SaTokenApikeyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SaTokenApikeyApplication.class, args);
    }
}
