package com.tangtang.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 订单服务启动类
 *
 * @author tangtang
 */
@SpringBootApplication
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
        System.out.println("""
            ========================================
            订单服务启动成功！
            访问地址: http://localhost:8082
            ========================================
            """);
    }
}
