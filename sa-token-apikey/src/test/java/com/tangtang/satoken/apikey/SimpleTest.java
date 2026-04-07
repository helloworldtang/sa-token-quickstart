package com.tangtang.satoken.apikey;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 简单的启动测试
 */
@SpringBootTest
public class SimpleTest {

    @Test
    public void contextLoads() {
        // 如果能运行到这里，说明 Spring 上下文加载成功
        assertTrue(true, "Spring 上下文加载成功");
    }
}
