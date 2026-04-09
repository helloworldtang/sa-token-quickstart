package com.tangtang.apisign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sa-Token API 接口参数签名示例启动类
 */
@SpringBootApplication
public class ApiSignApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiSignApplication.class, args);
        System.out.println("===========================================");
        System.out.println("  Sa-Token API Sign 示例已启动");
        System.out.println("  接口文档: http://localhost:8083");
        System.out.println("  健康检查: http://localhost:8083/actuator/health");
        System.out.println("===========================================");
    }
}
