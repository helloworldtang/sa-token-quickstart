package com.tangtang.satoken;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sa-Token 快速入门启动类
 * 
 * @author 码骨丹心
 */
@SpringBootApplication
public class SaTokenQuickStartApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaTokenQuickStartApplication.class, args);
        System.out.println("✅ Sa-Token 快速入门项目启动成功！");
        System.out.println("📖 API 文档: http://localhost:8080/doc.html");
        System.out.println("🔐 登录接口: POST http://localhost:8080/auth/login");
        System.out.println("   参数: username=admin, password=123456");
        System.out.println("📖 其他接口:");
        System.out.println("   登出: POST http://localhost:8080/auth/logout");
        System.out.println("   检查登录: GET http://localhost:8080/auth/isLogin");
        System.out.println("   用户信息: GET http://localhost:8080/auth/info");
    }
}
