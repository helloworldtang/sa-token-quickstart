package com.tangtang.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 网关服务启动类
 *
 * @author tangtang
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
        System.out.println("""
            ========================================
            网关服务启动成功！
            访问地址: http://localhost:8080
            ========================================
            """);
    }
}
