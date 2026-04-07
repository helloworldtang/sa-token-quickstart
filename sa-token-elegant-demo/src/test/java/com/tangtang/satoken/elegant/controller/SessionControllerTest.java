package com.tangtang.satoken.elegant.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangtang.satoken.elegant.model.dto.KickoutRequest;
import com.tangtang.satoken.elegant.model.dto.LoginRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SessionController 集成测试
 * <p>
 * 优雅点：
 * 1. 测试会话管理功能
 * 2. 测试踢人权限控制
 * 3. 测试参数校验
 *
 * @author Agent唐
 */
@SpringBootTest
@AutoConfigureMockMvc
class SessionControllerTest {

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
     * 测试获取在线会话列表
     */
    @Test
    void testGetOnlineSessions() throws Exception {
        mockMvc.perform(get("/admin/online-sessions")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试普通用户无法查看会话列表
     */
    @Test
    void testGetOnlineSessionsByUser() throws Exception {
        mockMvc.perform(get("/admin/online-sessions")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    /**
     * 测试未登录时查看会话列表
     */
    @Test
    void testGetOnlineSessionsNotLogin() throws Exception {
        mockMvc.perform(get("/admin/online-sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    /**
     * 测试根据用户ID踢人
     */
    @Test
    void testKickoutByUserId() throws Exception {
        mockMvc.perform(post("/admin/kickout/2")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("踢人成功"));
    }

    /**
     * 测试根据用户ID踢人（带原因）
     */
    @Test
    void testKickoutByUserIdWithReason() throws Exception {
        KickoutRequest request = KickoutRequest.builder()
                .reason("违反用户协议")
                .build();

        mockMvc.perform(post("/admin/kickout/2")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("踢人成功"));
    }

    /**
     * 测试普通用户无法根据用户ID踢人
     */
    @Test
    void testKickoutByUserIdByUser() throws Exception {
        mockMvc.perform(post("/admin/kickout/1")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value(containsString("角色不足")));
    }

    /**
     * 测试未登录时踢人
     */
    @Test
    void testKickoutByUserIdNotLogin() throws Exception {
        mockMvc.perform(post("/admin/kickout/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}