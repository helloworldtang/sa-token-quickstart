package com.tangtang.satoken.elegant.service;

import cn.dev33.satoken.stp.StpUtil;
import com.tangtang.satoken.elegant.exception.BusinessException;
import com.tangtang.satoken.elegant.model.dto.KickoutRequest;
import com.tangtang.satoken.elegant.model.dto.LoginRequest;
import com.tangtang.satoken.elegant.model.dto.LoginResponse;
import com.tangtang.satoken.elegant.model.dto.OnlineSessionDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthService 单元测试
 * <p>
 * 优雅点：
 * 1. 测试Sa-Token的登录、登出功能
 * 2. 测试异常处理
 * 3. 使用@BeforeEach和@AfterEach管理测试状态
 * 4. 断言清晰，覆盖各种场景
 *
 * @author Agent唐
 */
@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    /**
     * 每个测试后清理登录状态
     */
    @AfterEach
    void tearDown() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    /**
     * 测试成功登录
     */
    @Test
    void testLoginSuccess() {
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("123456")
                .build();

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("admin", response.getUsername());
        assertEquals("管理员", response.getNickname());
        assertTrue(StpUtil.isLogin());
    }

    /**
     * 测试用户名不存在
     */
    @Test
    void testLoginUserNotFound() {
        LoginRequest request = LoginRequest.builder()
                .username("notexist")
                .password("123456")
                .build();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.login(request)
        );

        assertEquals("用户不存在", exception.getMessage());
    }

    /**
     * 测试密码错误
     */
    @Test
    void testLoginWrongPassword() {
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("wrong")
                .build();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.login(request)
        );

        assertEquals("密码错误", exception.getMessage());
    }

    /**
     * 测试登出
     */
    @Test
    void testLogout() {
        // 先登录
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("123456")
                .build();
        authService.login(request);
        assertTrue(StpUtil.isLogin());

        // 登出
        authService.logout();
        assertFalse(StpUtil.isLogin());
    }

    /**
     * 测试获取当前用户
     */
    @Test
    void testGetCurrentUser() {
        // 先登录
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("123456")
                .build();
        authService.login(request);

        // 获取当前用户
        LoginResponse response = authService.getCurrentUser();

        assertNotNull(response);
        assertEquals("admin", response.getUsername());
        assertTrue(StpUtil.isLogin());
    }

    /**
     * 测试未登录时获取当前用户
     */
    @Test
    void testGetCurrentUserNotLogin() {
        // 未登录时应该抛出NotLoginException
        assertThrows(cn.dev33.satoken.exception.NotLoginException.class,
                () -> authService.getCurrentUser());
    }

    /**
     * 测试踢人下线
     */
    @Test
    void testKickout() {
        // 先登录，获取Token
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("123456")
                .build();
        LoginResponse response = authService.login(request);
        String token = response.getToken();

        assertTrue(StpUtil.isLogin());

        // 踢人
        authService.kickout(token);

        // Token应该失效
        assertFalse(StpUtil.isLogin());
    }

    /**
     * 测试根据用户ID踢人
     */
    @Test
    void testKickoutByUserId() {
        // 登录普通用户
        LoginRequest userRequest = LoginRequest.builder()
                .username("user")
                .password("123456")
                .build();
        LoginResponse userResponse = authService.login(userRequest);

        // 根据用户ID踢人
        KickoutRequest kickoutRequest = KickoutRequest.builder()
                .reason("违反用户协议")
                .build();
        authService.kickoutByUserId(2L, kickoutRequest);

        // 验证用户被踢
        assertFalse(isUserOnline(userResponse.getToken()));
    }

    /**
     * 测试根据会话ID踢人
     */
    @Test
    void testKickoutBySessionId() {
        // 登录普通用户
        LoginRequest userRequest = LoginRequest.builder()
                .username("user")
                .password("123456")
                .build();
        LoginResponse userResponse = authService.login(userRequest);
        String userToken = userResponse.getToken();

        // 获取在线会话列表，找到该用户的会话ID
        List<OnlineSessionDTO> sessions = authService.getOnlineSessions();
        String sessionId = sessions.stream()
                .filter(s -> s.getUserId().equals(2L))
                .map(OnlineSessionDTO::getSessionId)
                .findFirst()
                .orElse(null);

        assertNotNull(sessionId);

        // 根据会话ID踢人
        authService.kickoutBySessionId(sessionId, null);

        // 验证会话被踢
        assertFalse(isUserOnline(userToken));
    }

    /**
     * 测试获取在线会话列表
     */
    @Test
    void testGetOnlineSessions() {
        // 登录两个用户
        LoginRequest adminRequest = LoginRequest.builder()
                .username("admin")
                .password("123456")
                .build();
        authService.login(adminRequest);

        LoginRequest userRequest = LoginRequest.builder()
                .username("user")
                .password("123456")
                .build();
        authService.login(userRequest);

        // 获取在线会话列表
        List<OnlineSessionDTO> sessions = authService.getOnlineSessions();

        // 应该有两个会话
        assertNotNull(sessions);
        assertEquals(2, sessions.size());
    }

    /**
     * 检查用户是否在线
     */
    private boolean isUserOnline(String token) {
        if (token == null) {
            return false;
        }
        try {
            // 尝试获取会话，如果成功则在线
            Object session = StpUtil.getSessionByToken(token);
            return session != null;
        } catch (Exception e) {
            return false;
        }
    }
}
