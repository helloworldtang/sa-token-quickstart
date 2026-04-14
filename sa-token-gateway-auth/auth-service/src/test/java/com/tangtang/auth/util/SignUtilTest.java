package com.tangtang.auth.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 签名工具类测试
 *
 * @author tangtang
 */
class SignUtilTest {

    @Test
    void testGenerateSign() {
        String apiKey = "test_api_key_123";
        String secretKey = "test_secret_key_456";
        Long timestamp = System.currentTimeMillis() / 1000; // 使用秒时间戳

        String sign = SignUtil.generateSign(apiKey, secretKey, timestamp);

        assertNotNull(sign, "生成的签名不应为空");
        assertFalse(sign.isEmpty(), "生成的签名不应为空字符串");
        assertTrue(sign.length() > 0, "生成的签名长度应该大于0");
    }

    @Test
    void testCheckSign() {
        String apiKey = "test_api_key_123";
        String secretKey = "test_secret_key_456";
        Long timestamp = System.currentTimeMillis() / 1000; // 使用秒时间戳

        // 生成签名
        String sign = SignUtil.generateSign(apiKey, secretKey, timestamp);

        // 验证签名
        boolean result = SignUtil.checkSign(apiKey, secretKey, timestamp, sign);

        assertTrue(result, "签名验证应该通过");
    }

    @Test
    void testCheckSignWithWrongSign() {
        String apiKey = "test_api_key_123";
        String secretKey = "test_secret_key_456";
        Long timestamp = System.currentTimeMillis() / 1000; // 使用秒时间戳

        // 使用错误的签名
        String wrongSign = "wrong_signature";

        boolean result = SignUtil.checkSign(apiKey, secretKey, timestamp, wrongSign);

        assertFalse(result, "错误的签名应该验证失败");
    }

    @Test
    void testCheckSignWithDifferentSecretKey() {
        String apiKey = "test_api_key_123";
        String secretKey1 = "test_secret_key_456";
        String secretKey2 = "different_secret_key";
        Long timestamp = System.currentTimeMillis();

        // 使用 secretKey1 生成签名
        String sign = SignUtil.generateSign(apiKey, secretKey1, timestamp);

        // 使用 secretKey2 验证签名
        boolean result = SignUtil.checkSign(apiKey, secretKey2, timestamp, sign);

        assertFalse(result, "使用不同的密钥应该验证失败");
    }

    @Test
    void testSignConsistency() {
        String apiKey = "test_api_key_123";
        String secretKey = "test_secret_key_456";
        Long timestamp = System.currentTimeMillis() / 1000; // 使用秒时间戳

        // 生成两次签名
        String sign1 = SignUtil.generateSign(apiKey, secretKey, timestamp);
        String sign2 = SignUtil.generateSign(apiKey, secretKey, timestamp);

        // 相同的输入应该产生相同的签名
        assertEquals(sign1, sign2, "相同的输入应该产生相同的签名");
    }

    @Test
    void testSignUniqueness() {
        String apiKey = "test_api_key_123";
        String secretKey = "test_secret_key_456";

        // 使用不同的时间戳生成签名
        String sign1 = SignUtil.generateSign(apiKey, secretKey, 123456789L);
        String sign2 = SignUtil.generateSign(apiKey, secretKey, 123456790L);

        // 不同的时间戳应该产生不同的签名
        assertNotEquals(sign1, sign2, "不同的时间戳应该产生不同的签名");
    }
}