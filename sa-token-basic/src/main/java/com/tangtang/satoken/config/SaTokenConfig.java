package com.tangtang.satoken.config;

import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.util.SaResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 * 
 * 配置权限拦截器和全局过滤器
 * 
 * @author 码骨丹心
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 拦截器
     * 开启注解鉴权功能
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor())
                .addPathPatterns("/**")
                // 放行 Swagger 相关路径
                .excludePathPatterns(
                        "/doc.html",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/webjars/**"
                );
    }

    /**
     * 全局过滤器配置
     * 用于处理跨域、认证异常等
     */
    @Bean
    public SaServletFilter getSaServletFilter() {
        return new SaServletFilter()
                // 认证失败处理
                .setAuth(obj -> {
                    // 打印访问路径
                    System.out.println("---------- 进入 Sa-Token 全局认证 -----------");
                })
                // 异常处理
                .setError(e -> {
                    return SaResult.error(e.getMessage());
                });
    }
}
