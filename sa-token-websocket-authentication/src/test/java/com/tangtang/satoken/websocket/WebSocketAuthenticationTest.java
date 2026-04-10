package com.tangtang.satoken.websocket;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebSocket 鉴权集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("WebSocket 鉴权集成测试")
class WebSocketAuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        // 1. 管理员登录
        MvcResult adminLoginResult = mockMvc.perform(post("/auth/login")
                        .param("username", "admin")
                        .param("password", "123456"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode adminNode = objectMapper.readTree(adminLoginResult.getResponse().getContentAsString());
        adminToken = adminNode.get("data").get("token").asText();

        // 2. 普通用户登录
        MvcResult userLoginResult = mockMvc.perform(post("/auth/login")
                        .param("username", "user")
                        .param("password", "123456"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode userNode = objectMapper.readTree(userLoginResult.getResponse().getContentAsString());
        userToken = userNode.get("data").get("token").asText();
    }

    @Test
    @DisplayName("测试1: HTTP 登录认证")
    void testHttpLogin() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .param("username", "admin")
                        .param("password", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.userId").value(10001));

        // 测试错误密码
        mockMvc.perform(post("/auth/login")
                        .param("username", "admin")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("测试2: 检查登录状态")
    void testCheckLoginStatus() throws Exception {
        mockMvc.perform(get("/auth/isLogin")
                        .header("satoken", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(get("/auth/isLogin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("测试3: 获取用户信息")
    void testGetUserInfo() throws Exception {
        mockMvc.perform(get("/auth/info")
                        .header("satoken", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(10001))
                .andExpect(jsonPath("$.data.token").value(adminToken));
    }

    @Test
    @DisplayName("测试4: 获取 WebSocket 连接地址")
    void testGetWebSocketUrl() throws Exception {
        MvcResult result = mockMvc.perform(get("/auth/ws-url")
                        .header("satoken", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        assertTrue(node.get("data").get("wsUrl").asText().contains("/ws?token="));
        assertEquals(adminToken, node.get("data").get("token").asText());
    }

    @Test
    @DisplayName("测试5: 登出功能")
    void testLogout() throws Exception {
        // 1. 登录 test 用户
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .param("username", "test")
                        .param("password", "123456"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode loginNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String testToken = loginNode.get("data").get("token").asText();

        // 2. 验证 token 有效
        mockMvc.perform(get("/auth/info")
                        .header("satoken", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 3. 登出 test 用户（使用正确的 token）
        mockMvc.perform(post("/auth/logout")
                        .header("satoken", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 4. 验证登出后 token 失效
        mockMvc.perform(get("/auth/info")
                        .header("satoken", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("测试6: 在线用户查询")
    void testOnlineUsers() throws Exception {
        mockMvc.perform(get("/message/online-users")
                        .header("satoken", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.count").exists())
                .andExpect(jsonPath("$.data.users").isArray());
    }

    @Test
    @DisplayName("测试7: 服务状态查询")
    void testServiceStatus() throws Exception {
        mockMvc.perform(get("/message/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.onlineCount").exists())
                .andExpect(jsonPath("$.data.websocketEnabled").value(true))
                .andExpect(jsonPath("$.data.wsEndpoint").value("/ws"));
    }

    @Test
    @DisplayName("测试8: 发送广播消息")
    void testBroadcastMessage() throws Exception {
        mockMvc.perform(post("/message/broadcast")
                        .header("satoken", adminToken)
                        .param("content", "测试广播消息"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("广播发送成功"));
    }

    @Test
    @DisplayName("测试9: 未登录用户查询在线用户")
    void testOnlineUsersWithoutLogin() throws Exception {
        mockMvc.perform(get("/message/online-users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("测试10: 多用户登录隔离")
    void testMultiUserLoginIsolation() throws Exception {
        mockMvc.perform(get("/auth/info")
                        .header("satoken", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(10001));

        mockMvc.perform(get("/auth/info")
                        .header("satoken", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(10002));
    }

    @Test
    @DisplayName("测试11: Token 无效处理")
    void testTokenExpiry() throws Exception {
        mockMvc.perform(get("/auth/info")
                        .header("satoken", "xxxx-invalid-token-xxxx"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}
