package com.tangtang.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 用户服务启动类
 *
 * @author tangtang
 */
@SpringBootApplication
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
        System.out.println("""
            ========================================
            用户服务启动成功！
            访问地址: http://localhost:8081
            Swagger文档: http://localhost:8081/doc.html
            ========================================
            """);
    }
}
