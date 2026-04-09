package com.tangtang.apisign.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * SignClientHelper 单元测试
 *
 * <p>测试覆盖：
 * <ul>
 *   <li>签名参数完整性（timestamp/nonce/sign 字段是否追加）</li>
 *   <li>签名幂等性：相同业务参数 + 不同 nonce/timestamp → sign 不同</li>
 *   <li>签名正确性：手动构造期望 sign，与工具类生成结果比对</li>
 *   <li>MD5 结果正确性</li>
 *   <li>queryString 拼接格式</li>
 * </ul>
 */
@DisplayName("SignClientHelper 签名工具类测试")
class SignClientHelperTest {

    private static final String SECRET_KEY = "test-secret-key-2026";

    // ===================== addSignParams =====================

    @Test
    @DisplayName("追加签名参数后，结果中必须包含 timestamp/nonce/sign 三个字段")
    void addSignParams_shouldContainRequiredFields() {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", 1001);
        params.put("amount", 5000);

        Map<String, Object> signed = SignClientHelper.addSignParams(params, SECRET_KEY);

        assertThat(signed).containsKey("timestamp");
        assertThat(signed).containsKey("nonce");
        assertThat(signed).containsKey("sign");
    }

    @Test
    @DisplayName("原始业务参数仍在结果中")
    void addSignParams_shouldPreserveBizParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", 1001L);
        params.put("amount", 5000L);

        Map<String, Object> signed = SignClientHelper.addSignParams(params, SECRET_KEY);

        assertThat(signed).containsEntry("userId", 1001L);
        assertThat(signed).containsEntry("amount", 5000L);
    }

    @Test
    @DisplayName("两次调用，nonce 不同，sign 也不同（随机性）")
    void addSignParams_differentNonceYieldsDifferentSign() {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", 1001);

        Map<String, Object> signed1 = SignClientHelper.addSignParams(params, SECRET_KEY);
        Map<String, Object> signed2 = SignClientHelper.addSignParams(params, SECRET_KEY);

        // nonce 应该不同
        assertThat(signed1.get("nonce")).isNotEqualTo(signed2.get("nonce"));
        // sign 也应该不同（因为 nonce 参与签名）
        assertThat(signed1.get("sign")).isNotEqualTo(signed2.get("sign"));
    }

    @Test
    @DisplayName("密钥不同时，相同参数生成不同 sign")
    void addSignParams_differentSecretKeyYieldsDifferentSign() {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", 1001);
        params.put("nonce", "fixed-nonce");
        params.put("timestamp", "1712556000000");

        // 两个密钥生成的 sign 不同
        Map<String, Object> signed1 = SignClientHelper.addSignParams(params, "key-aaa");
        Map<String, Object> signed2 = SignClientHelper.addSignParams(params, "key-bbb");

        assertThat(signed1.get("sign")).isNotEqualTo(signed2.get("sign"));
    }

    @Test
    @DisplayName("sign 值为 32 位小写十六进制字符串（MD5 格式）")
    void addSignParams_signShouldBeMd5Format() {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", 999);

        Map<String, Object> signed = SignClientHelper.addSignParams(params, SECRET_KEY);
        String sign = (String) signed.get("sign");

        assertThat(sign).hasSize(32);
        assertThat(sign).matches("[0-9a-f]{32}");
    }

    // ===================== md5 =====================

    @ParameterizedTest
    @CsvSource({
        "hello,5d41402abc4b2a76b9719d911017c592",
        "abc,900150983cd24fb0d6963f7d28e17f72",
        "sa-token,88892bd9e60782279a382f836a3300fe"
    })
    @DisplayName("MD5 计算结果与预期一致")
    void md5_shouldMatchKnownValues(String input, String expected) {
        assertThat(SignClientHelper.md5(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("MD5 结果始终为 32 位小写")
    void md5_alwaysReturns32LowercaseHex() {
        String result = SignClientHelper.md5("any string 12345");
        assertThat(result).hasSize(32);
        assertThat(result).isLowerCase();
    }

    // ===================== 签名算法正确性验证 =====================

    @Test
    @DisplayName("手动构造期望 sign，与工具类结果一致（算法一致性）")
    void addSignParams_signMatchesManualCalculation() {
        // 固定所有参数，确定性验证算法正确
        String fixedTimestamp = "1712556000000";
        String fixedNonce = "abc123def456";
        Map<String, Object> params = new HashMap<>();
        params.put("userId", "1001");
        params.put("amount", "5000");
        params.put("timestamp", fixedTimestamp);
        params.put("nonce", fixedNonce);

        // 手动计算期望签名
        // 字典序排列: amount, nonce, timestamp, userId → 拼接 key=xxx
        String rawStr = "amount=5000&nonce=abc123def456&timestamp=1712556000000&userId=1001&key=" + SECRET_KEY;
        String expectedSign = SignClientHelper.md5(rawStr);

        // 用工具类生成（因为 params 里已有 timestamp/nonce，不会再随机生成）
        Map<String, Object> signed = SignClientHelper.addSignParams(params, SECRET_KEY);

        assertThat(signed.get("sign")).isEqualTo(expectedSign);
    }

    // ===================== toQueryString =====================

    @Test
    @DisplayName("toQueryString 正确拼接 k=v&k=v 格式")
    void toQueryString_shouldProduceCorrectFormat() {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", "1001");
        params.put("sign", "abc");

        String qs = SignClientHelper.toQueryString(params);

        assertThat(qs).contains("userId=1001");
        assertThat(qs).contains("sign=abc");
        assertThat(qs).contains("&");
    }

    @Test
    @DisplayName("空 Map 返回空字符串")
    void toQueryString_emptyMapReturnsEmpty() {
        assertThat(SignClientHelper.toQueryString(new HashMap<>())).isEmpty();
    }
}
