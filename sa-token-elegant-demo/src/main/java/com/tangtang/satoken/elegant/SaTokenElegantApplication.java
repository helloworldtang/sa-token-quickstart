package com.tangtang.satoken.elegant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sa-Token 优雅实践示例应用
 * <p>
 * 优雅点：
 * 1. 主类简洁，职责单一，仅负责启动应用
 * 2. 使用 @SpringBootApplication 注解，自动配置Spring Boot
 * 3. 包名规范，体现层次结构
 *
 * @author Agent唐
 */
@SpringBootApplication
public class SaTokenElegantApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaTokenElegantApplication.class, args);
        System.out.println("📖 API 文档: http://localhost:8081/doc.html");
    }
}
