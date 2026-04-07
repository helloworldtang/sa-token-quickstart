package com.tangtang.satoken.elegant.service;

import com.tangtang.satoken.elegant.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserService 单元测试
 * <p>
 * 优雅点：
 * 1. 使用JUnit 5，测试更简洁
 * 2. 使用Spring Boot Test，自动注入Bean
 * 3. 断言清晰，易于理解
 * 4. 测试覆盖度高，保证代码质量
 *
 * @author Agent唐
 */
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    /**
     * 测试根据用户名获取用户
     */
    @Test
    void testGetByUsername() {
        User admin = userService.getByUsername("admin");
        assertNotNull(admin);
        assertEquals("admin", admin.getUsername());
        assertEquals("管理员", admin.getNickname());
    }

    /**
     * 测试获取不存在的用户
     */
    @Test
    void testGetByUsernameNotFound() {
        User user = userService.getByUsername("notexist");
        assertNull(user);
    }

    /**
     * 测试根据ID获取用户
     */
    @Test
    void testGetById() {
        User user = userService.getById(1L);
        assertNotNull(user);
        assertEquals("admin", user.getUsername());
    }

    /**
     * 测试密码校验
     */
    @Test
    void testCheckPassword() {
        boolean result = userService.checkPassword("123456", "123456");
        assertTrue(result);
    }

    /**
     * 测试密码校验失败
     */
    @Test
    void testCheckPasswordFail() {
        boolean result = userService.checkPassword("wrong", "123456");
        assertFalse(result);
    }

    /**
     * 测试普通用户
     */
    @Test
    void testGetRegularUser() {
        User user = userService.getByUsername("user");
        assertNotNull(user);
        assertEquals("user", user.getUsername());
        assertEquals("普通用户", user.getNickname());
        assertEquals("user", user.getRoles());
        assertEquals("user:view", user.getPermissions());
    }
}
