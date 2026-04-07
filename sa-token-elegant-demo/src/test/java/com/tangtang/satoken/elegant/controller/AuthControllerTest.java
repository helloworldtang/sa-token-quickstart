package com.tangtang.satoken.elegant.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangtang.satoken.elegant.model.dto.LoginRequest;
import com.tangtang.satoken.elegant.model.dto.LoginResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 集成测试
 * <p>
 * 优雅点：
 * 1. 使用MockMvc测试HTTP接口
 * 2. 测试完整的请求响应流程
 * 3. 测试认证和权限控制
 * 4. 验证响应格式和状态码
 *
 * @author Agent唐
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;

    /**
     * 每个测试前登录
     */
    @BeforeEach
    void setUp() throws Exception {
        // 登录管理员
        LoginRequest adminRequest = LoginRequest.builder()
                .username("admin")
                .password("123456")
                .build();

        String adminResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        adminToken = objectMapper.readTree(adminResponse)
                .path("data")
                .path("token")
                .asText();

        // 登录普通用户
        LoginRequest userRequest = LoginRequest.builder()
                .username("user")
                .password("123456")
                .build();

        String userResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        userToken = objectMapper.readTree(userResponse)
                .path("data")
                .path("token")
                .asText();
    }

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
    void testLoginSuccess() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("123456")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.nickname").value("管理员"));
    }

    /**
     * 测试登录失败 - 用户名不存在
     */
    @Test
    void testLoginUserNotFound() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("notexist")
                .password("123456")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    /**
     * 测试登录失败 - 密码错误
     */
    @Test
    void testLoginWrongPassword() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("wrong")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("密码错误"));
    }

    /**
     * 测试登出
     */
    @Test
    void testLogout() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登出成功"));
    }

    /**
     * 测试未登录时登出
     */
    @Test
    void testLogoutNotLogin() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    /**
     * 测试获取当前用户
     */
    @Test
    void testGetCurrentUser() throws Exception {
        mockMvc.perform(get("/auth/current")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.nickname").value("管理员"));
    }

    /**
     * 测试未登录时获取当前用户
     */
    @Test
    void testGetCurrentUserNotLogin() throws Exception {
        mockMvc.perform(get("/auth/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    /**
     * 测试管理员踢人
     */
    @Test
    void testKickoutByAdmin() throws Exception {
        mockMvc.perform(post("/auth/kickout")
                        .header("Authorization", adminToken)
                        .param("token", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("踢人成功"));
    }

    /**
     * 测试普通用户踢人 - 应该失败
     */
    @Test
    void testKickoutByUser() throws Exception {
        mockMvc.perform(post("/auth/kickout")
                        .header("Authorization", userToken)
                        .param("token", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value(containsString("角色不足")));
    }
}
