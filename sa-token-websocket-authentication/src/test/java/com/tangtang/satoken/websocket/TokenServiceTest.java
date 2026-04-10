package com.tangtang.satoken.websocket;

import cn.dev33.satoken.dao.SaTokenDao;
import com.tangtang.satoken.websocket.interceptor.WebSocketAuthInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebSocket 认证组件测试
 */
@SpringBootTest
@DisplayName("WebSocket 认证组件测试")
class TokenServiceTest {

    @Autowired(required = false)
    private WebSocketAuthInterceptor authInterceptor;

    @Autowired
    private SaTokenDao saTokenDao;

    @Test
    @DisplayName("测试1: SaTokenDao 实例化成功")
    void testSaTokenDaoExists() {
        assertNotNull(saTokenDao);
    }

    @Test
    @DisplayName("测试2: WebSocketAuthInterceptor 实例化成功")
    void testInterceptorExists() {
        assertNotNull(authInterceptor);
    }
}
