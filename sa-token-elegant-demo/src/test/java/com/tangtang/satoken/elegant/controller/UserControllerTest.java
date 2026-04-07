package com.tangtang.satoken.elegant.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangtang.satoken.elegant.model.User;
import com.tangtang.satoken.elegant.model.dto.LoginRequest;
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
 * UserController 集成测试
 * <p>
 * 优雅点：
 * 1. 测试权限控制（登录、角色、权限）
 * 2. 测试增删改查完整流程
 * 3. 测试不同角色的权限差异
 * 4. 验证Sa-Token注解是否生效
 *
 * @author Agent唐
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

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
     * 测试获取用户列表（需要登录）
     */
    @Test
    void testListWithLogin() throws Exception {
        mockMvc.perform(get("/user/list")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))));
    }

    /**
     * 测试未登录时获取用户列表
     */
    @Test
    void testListWithoutLogin() throws Exception {
        mockMvc.perform(get("/user/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    /**
     * 测试查看用户详情（有权限）
     */
    @Test
    void testGetByIdWithPermission() throws Exception {
        mockMvc.perform(get("/user/1")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    /**
     * 测试普通用户查看详情（无权限）
     */
    @Test
    void testGetByIdWithoutPermission() throws Exception {
        mockMvc.perform(get("/user/1")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value(containsString("权限不足")));
    }

    /**
     * 测试新增用户（有权限）
     */
    @Test
    void testAddWithPermission() throws Exception {
        User newUser = User.builder()
                .id(3L)
                .username("newuser")
                .password("123456")
                .nickname("新用户")
                .roles("user")
                .permissions("user:view")
                .build();

        mockMvc.perform(post("/user")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("新增成功"));
    }

    /**
     * 测试普通用户新增（无权限）
     */
    @Test
    void testAddWithoutPermission() throws Exception {
        User newUser = User.builder()
                .id(3L)
                .username("newuser")
                .password("123456")
                .nickname("新用户")
                .roles("user")
                .permissions("user:view")
                .build();

        mockMvc.perform(post("/user")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value(containsString("权限不足")));
    }

    /**
     * 测试修改用户（有权限）
     */
    @Test
    void testUpdateWithPermission() throws Exception {
        User updateUser = User.builder()
                .id(1L)
                .username("admin")
                .nickname("超级管理员")
                .roles("admin")
                .permissions("user:add,user:edit,user:delete,user:view")
                .build();

        mockMvc.perform(put("/user/1")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("修改成功"));
    }

    /**
     * 测试普通用户修改（无权限）
     */
    @Test
    void testUpdateWithoutPermission() throws Exception {
        User updateUser = User.builder()
                .id(1L)
                .username("admin")
                .nickname("超级管理员")
                .roles("admin")
                .permissions("user:add,user:edit,user:delete,user:view")
                .build();

        mockMvc.perform(put("/user/1")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value(containsString("权限不足")));
    }

    /**
     * 测试删除用户（有权限）
     */
    @Test
    void testDeleteWithPermission() throws Exception {
        mockMvc.perform(delete("/user/1")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));
    }

    /**
     * 测试普通用户删除（无权限）
     */
    @Test
    void testDeleteWithoutPermission() throws Exception {
        mockMvc.perform(delete("/user/1")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value(containsString("权限不足")));
    }

    /**
     * 测试获取当前登录用户ID
     */
    @Test
    void testGetCurrentLoginId() throws Exception {
        mockMvc.perform(get("/user/current-id")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));
    }

    /**
     * 测试检查权限
     */
    @Test
    void testCheckPermission() throws Exception {
        // 管理员有user:add权限
        mockMvc.perform(get("/user/check-permission")
                        .header("Authorization", adminToken)
                        .param("permission", "user:add"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        // 普通用户没有user:add权限
        mockMvc.perform(get("/user/check-permission")
                        .header("Authorization", userToken)
                        .param("permission", "user:add"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));
    }
}
