package com.tangtang.satoken.apikey;

import cn.dev33.satoken.apikey.model.ApiKeyModel;
import com.tangtang.satoken.apikey.limiter.ApiKeyRateLimiter;
import com.tangtang.satoken.apikey.limiter.RateLimiter;
import com.tangtang.satoken.apikey.service.ApiKeyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangtang.satoken.apikey.controller.AdminController;
import com.tangtang.satoken.apikey.limiter.ApiKeyRateLimiter.ApiKeyUsageStats;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API Key 集成测试
 *
 * 测试完整的 API Key 生命周期：
 * 1. 管理员登录
 * 2. 创建 API Key
 * 3. 使用 API Key 调用 API
 * 4. 限流功能测试
 * 5. 删除 API Key
 * 6. 管理员登出
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiKeyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private ApiKeyRateLimiter apiKeyRateLimiter;

    @Autowired
    private RateLimiter ipRateLimiter;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static String adminToken;
    private static String testApiKey;

    @BeforeAll
    public static void setup() {
        System.out.println("========== 开始 API Key 集成测试 ==========");
    }

    @AfterAll
    public static void teardown() {
        System.out.println("========== API Key 集成测试完成 ==========");
    }

    @AfterEach
    public void cleanup() {
        // 清理 Redis 中的限流计数
        if (testApiKey != null) {
            try {
                apiKeyRateLimiter.resetRateLimit(testApiKey);
                apiKeyRateLimiter.resetDailyLimit(testApiKey);
            } catch (Exception e) {
                // 忽略异常
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. 管理员登录测试")
    public void testAdminLogin() throws Exception {
        System.out.println("\n测试 1: 管理员登录");

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin");
        loginRequest.put("password", "admin123");

        MvcResult result = mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.tokenValue").exists())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        adminToken = (String) data.get("tokenValue");

        assertNotNull(adminToken, "管理员登录应该返回 token");
        assertTrue(adminToken.length() > 10, "Token 应该有足够的长度");
        System.out.println("✓ 管理员登录成功，Token: " + adminToken.substring(0, 20) + "...");
    }

    @Test
    @Order(2)
    @DisplayName("2. 获取管理员信息测试")
    public void testGetAdminInfo() throws Exception {
        System.out.println("\n测试 2: 获取管理员信息");

        mockMvc.perform(get("/admin/info")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.loginId").value(1001));

        System.out.println("✓ 获取管理员信息成功");
    }

    @Test
    @Order(3)
    @DisplayName("3. 创建 API Key 测试")
    public void testCreateApiKey() throws Exception {
        System.out.println("\n测试 3: 创建 API Key");

        Map<String, String> createRequest = new HashMap<>();
        createRequest.put("name", "测试API Key");

        MvcResult result = mockMvc.perform(post("/admin/apikey/create")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.apiKey").exists())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        testApiKey = (String) data.get("apiKey");

        assertNotNull(testApiKey, "创建 API Key 应该返回 API Key");
        assertTrue(testApiKey.startsWith("sk-"), "API Key 应该以 sk- 开头");
        assertTrue(testApiKey.length() > 30, "API Key 长度应该足够长");
        System.out.println("✓ API Key 创建成功: " + testApiKey);
    }

    @Test
    @Order(4)
    @DisplayName("4. 获取 API Key 列表测试")
    public void testListApiKeys() throws Exception {
        System.out.println("\n测试 4: 获取 API Key 列表");

        MvcResult result = mockMvc.perform(get("/admin/apikey/list")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        List<Map<String, Object>> apiKeyList = (List<Map<String, Object>>) responseMap.get("data");

        assertNotNull(apiKeyList, "API Key 列表不应该为 null");
        assertFalse(apiKeyList.isEmpty(), "API Key 列表应该包含至少一个 Key");
        assertTrue(apiKeyList.stream().anyMatch(k -> testApiKey.equals(k.get("apiKey"))),
                "API Key 列表应该包含刚创建的 Key");

        System.out.println("✓ 获取 API Key 列表成功，当前数量: " + apiKeyList.size());
    }

    @Test
    @Order(5)
    @DisplayName("5. 使用 API Key 调用聊天接口测试")
    public void testChatWithApiKey() throws Exception {
        System.out.println("\n测试 5: 使用 API Key 调用聊天接口");

        Map<String, String> chatRequest = new HashMap<>();
        chatRequest.put("message", "你好，这是一个测试消息");

        MvcResult result = mockMvc.perform(post("/api/v1/chat")
                        .header("Authorization", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.message.content").exists())
                .andExpect(header().exists("X-RateLimit-Limit-Minute"))
                .andExpect(header().exists("X-RateLimit-Remaining-Minute"))
                .andExpect(header().exists("X-RateLimit-Limit-Day"))
                .andExpect(header().exists("X-RateLimit-Remaining-Day"))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Map<String, Object> data = objectMapper.readValue(response, Map.class);

        assertNotNull(data.get("id"), "响应应该包含 id");
        System.out.println("✓ API Key 调用聊天接口成功，响应 ID: " + data.get("id"));
    }

    @Test
    @Order(6)
    @DisplayName("6. 使用 API Key 获取模型列表测试")
    public void testModelsWithApiKey() throws Exception {
        System.out.println("\n测试 6: 使用 API Key 获取模型列表");

        mockMvc.perform(get("/api/v1/models")
                        .header("Authorization", testApiKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.data").isArray())
                .andExpect(jsonPath("$.data.data.length").value(3));

        System.out.println("✓ 获取模型列表成功");
    }

    @Test
    @Order(7)
    @DisplayName("7. 使用无效 API Key 测试")
    public void testInvalidApiKey() throws Exception {
        System.out.println("\n测试 7: 使用无效 API Key");

        Map<String, String> chatRequest = new HashMap<>();
        chatRequest.put("message", "测试");

        mockMvc.perform(post("/api/v1/chat")
                        .header("Authorization", "sk-invalid-key-123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        System.out.println("✓ 无效 API Key 正确返回 401 错误");
    }

    @Test
    @Order(8)
    @DisplayName("8. 缺少 API Key 测试")
    public void testMissingApiKey() throws Exception {
        System.out.println("\n测试 8: 缺少 API Key");

        Map<String, String> chatRequest = new HashMap<>();
        chatRequest.put("message", "测试");

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        System.out.println("✓ 缺少 API Key 正确返回 401 错误");
    }

    @Test
    @Order(9)
    @DisplayName("9. 获取 API Key 使用统计测试")
    public void testGetApiKeyStats() throws Exception {
        System.out.println("\n测试 9: 获取 API Key 使用统计");

        MvcResult result = mockMvc.perform(get("/admin/apikey/" + testApiKey + "/stats")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.apiKey").value(testApiKey))
                .andExpect(jsonPath("$.data.minuteUsed").exists())
                .andExpect(jsonPath("$.data.minuteLimit").exists())
                .andExpect(jsonPath("$.data.minuteRemaining").exists())
                .andExpect(jsonPath("$.data.dailyUsed").exists())
                .andExpect(jsonPath("$.data.dailyLimit").exists())
                .andExpect(jsonPath("$.data.dailyRemaining").exists())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");

        System.out.println("✓ 获取 API Key 使用统计成功:");
        System.out.println("  - 每分钟使用: " + data.get("minuteUsed") + "/" + data.get("minuteLimit"));
        System.out.println("  - 每日使用: " + data.get("dailyUsed") + "/" + data.get("dailyLimit"));
    }

    @Test
    @Order(10)
    @DisplayName("10. API Key 限流测试（分钟级别）")
    public void testApiKeyRateLimit() throws Exception {
        System.out.println("\n测试 10: API Key 限流测试（分钟级别）");

        Map<String, String> chatRequest = new HashMap<>();
        chatRequest.put("message", "限流测试");

        final int[] successCount = {0};
        final int[] rateLimitCount = {0};

        // 快速发送多个请求测试限流
        for (int i = 0; i < 25; i++) {
            try {
                mockMvc.perform(post("/api/v1/chat")
                        .header("Authorization", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRequest)))
                        .andDo(result -> {
                            if (result.getResponse().getStatus() == 200) {
                                successCount[0]++;
                            } else if (result.getResponse().getStatus() == 429) {
                                rateLimitCount[0]++;
                            }
                        });
            } catch (Exception e) {
                // 忽略异常
            }
        }

        System.out.println("✓ 限流测试完成:");
        System.out.println("  - 成功请求: " + successCount[0] + "");
        System.out.println("  - 被限流请求: " + rateLimitCount[0] + "");

        // 至少应该有一些请求被限流
        assertTrue(rateLimitCount[0] > 0, "应该有请求被限流");
    }

    @Test
    @Order(11)
    @DisplayName("11. 重置 API Key 限流测试")
    public void testResetRateLimit() throws Exception {
        System.out.println("\n测试 11: 重置 API Key 限流");

        mockMvc.perform(post("/admin/apikey/" + testApiKey + "/reset-rate-limit")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        System.out.println("✓ 重置 API Key 限流成功");

        // 重置后应该可以再次请求
        Map<String, String> chatRequest = new HashMap<>();
        chatRequest.put("message", "重置后测试");
        mockMvc.perform(post("/api/v1/chat")
                        .header("authorization", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        System.out.println("✓ 重置后 API Key 可以正常使用");
    }

    @Test
    @Order(12)
    @DisplayName("12. 删除 API Key 测试")
    public void testDeleteApiKey() throws Exception {
        System.out.println("\n测试 12: 删除 API Key");

        mockMvc.perform(delete("/admin/apikey/" + testApiKey)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        System.out.println("✓ 删除 API Key 成功");

        // 删除后应该无法使用
        Map<String, String> chatRequest = new HashMap<>();
        chatRequest.put("message", "删除后测试");
        mockMvc.perform(post("/api/v1/chat")
                        .header("Authorization", testApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isUnauthorized());

        System.out.println("✓ 删除后 API Key 正确失效");
    }

    @Test
    @Order(13)
    @DisplayName("13. 管理员登出测试")
    public void testAdminLogout() throws Exception {
        System.out.println("\n测试 13: 管理员登出");

        mockMvc.perform(post("/admin/logout")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        System.out.println("✓ 管理员登出成功");
    }
}
