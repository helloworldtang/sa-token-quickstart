package com.tangtang.satoken.apikey.config;

import com.tangtang.satoken.apikey.interceptor.ApiKeyInterceptor;
import com.tangtang.satoken.apikey.limiter.ApiKeyRateLimiter;
import com.tangtang.satoken.apikey.limiter.RateLimiter;
import cn.dev33.satoken.apikey.template.SaApiKeyTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApiKeyInterceptor apiKeyInterceptor;

    public WebMvcConfig(SaApiKeyTemplate apikeyTemplate,
                        RateLimiter ipRateLimiter,
                        ApiKeyRateLimiter apikeyRateLimiter) {
        // ★ 拦截器 new 在构造器里，手动注入依赖
        this.apiKeyInterceptor = new ApiKeyInterceptor(apikeyTemplate, ipRateLimiter);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 只有 /api/v1/** 需要 API Key 认证
        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/api/v1/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
