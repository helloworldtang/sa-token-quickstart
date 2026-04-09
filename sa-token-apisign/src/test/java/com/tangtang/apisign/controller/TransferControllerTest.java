package com.tangtang.apisign.controller;

import cn.dev33.satoken.sign.template.SaSignUtil;
import com.tangtang.apisign.util.SignClientHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TransferController 集成测试
 *
 * <p>验证签名校验的完整链路：合法请求通过、篡改参数被拒绝、签名缺失被拒绝等
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("TransferController 签名校验集成测试")
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${sa-token.sign.secret-key}")
    private String secretKey;

    /**
     * 构建合法的签名请求参数
     */
    private Map<String, Object> buildValidParams(Map<String, Object> bizParams) {
        return SignClientHelper.addSignParams(bizParams, secretKey);
    }

    @Test
    @DisplayName("合法签名请求 → 200 成功")
    void submitTransfer_withValidSign_shouldReturn200() throws Exception {
        Map<String, Object> bizParams = new HashMap<>();
        bizParams.put("userId", "1001");
        bizParams.put("amount", "5000");
        bizParams.put("remark", "测试转账");

        Map<String, Object> signedParams = buildValidParams(bizParams);
        String qs = SignClientHelper.toQueryString(signedParams);

        mockMvc.perform(get("/api/transfer/submit?" + qs))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.userId").value(1001));
    }

    @Test
    @DisplayName("缺少 sign 参数 → 签名校验失败")
    void submitTransfer_withoutSign_shouldFail() throws Exception {
        mockMvc.perform(get("/api/transfer/submit")
                        .param("userId", "1001")
                        .param("amount", "5000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("sign 值错误 → 签名校验失败")
    void submitTransfer_withWrongSign_shouldFail() throws Exception {
        Map<String, Object> bizParams = new HashMap<>();
        bizParams.put("userId", "1001");
        bizParams.put("amount", "5000");

        Map<String, Object> signedParams = buildValidParams(bizParams);
        signedParams.put("sign", "wrong_sign_value_here");
        String qs = SignClientHelper.toQueryString(signedParams);

        mockMvc.perform(get("/api/transfer/submit?" + qs))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("篡改参数（修改 amount）→ sign 失效，校验失败")
    void submitTransfer_withTamperedParam_shouldFail() throws Exception {
        Map<String, Object> bizParams = new HashMap<>();
        bizParams.put("userId", "1001");
        bizParams.put("amount", "5000");

        Map<String, Object> signedParams = buildValidParams(bizParams);
        // 签名后篡改 amount（黑客行为模拟）
        signedParams.put("amount", "999999");
        String qs = SignClientHelper.toQueryString(signedParams);

        mockMvc.perform(get("/api/transfer/submit?" + qs))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("时间戳过期（超出 15 分钟）→ 签名校验失败")
    void submitTransfer_withExpiredTimestamp_shouldFail() throws Exception {
        Map<String, Object> bizParams = new HashMap<>();
        bizParams.put("userId", "1001");
        bizParams.put("amount", "5000");

        Map<String, Object> signedParams = buildValidParams(bizParams);
        // 将时间戳设为 20 分钟前（超出 15 分钟窗口）
        long expiredTimestamp = System.currentTimeMillis() - 20 * 60 * 1000L;
        signedParams.put("timestamp", String.valueOf(expiredTimestamp));

        // 时间戳改了，需要重新计算 sign（使用过期时间戳计算签名）
        // 模拟黑客用过期请求重放的场景
        Map<String, Object> replayParams = new HashMap<>(bizParams);
        replayParams.put("timestamp", String.valueOf(expiredTimestamp));
        replayParams.put("nonce", signedParams.get("nonce"));
        Map<String, Object> replaySignedParams = SignClientHelper.addSignParams(replayParams, secretKey);
        // 但实际上 timestamp 已经超出允许范围
        replaySignedParams.put("timestamp", String.valueOf(expiredTimestamp));

        // 重新计算 sign（以旧 timestamp）
        String qs = SignClientHelper.toQueryString(replaySignedParams);

        mockMvc.perform(get("/api/transfer/submit?" + qs))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("合法签名查询余额 → 200 成功")
    void queryBalance_withValidSign_shouldReturn200() throws Exception {
        Map<String, Object> bizParams = new HashMap<>();
        bizParams.put("userId", "1001");

        Map<String, Object> signedParams = buildValidParams(bizParams);
        String qs = SignClientHelper.toQueryString(signedParams);

        mockMvc.perform(get("/api/transfer/balance?" + qs))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.balance").value(99999));
    }

    @Test
    @DisplayName("demo-sign 接口返回完整签名参数")
    void demoSign_shouldReturnSignedParams() throws Exception {
        mockMvc.perform(get("/api/transfer/demo-sign")
                        .param("userId", "1001")
                        .param("amount", "5000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.queryString").isString())
                .andExpect(jsonPath("$.data.exampleUrl").isString());
    }
}
