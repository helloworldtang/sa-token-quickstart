package com.tangtang.auth.util;

import cn.hutool.crypto.SecureUtil;

import java.util.Map;
import java.util.TreeMap;

/**
 * API 签名工具类
 * 用于 API Key 认证场景的签名生成与验证
 *
 * @author tangtang
 */
public class SignUtil {

    /**
     * 生成签名
     *
     * @param apiKey    API Key
     * @param secretKey 秘钥
     * @param timestamp 时间戳
     * @return 签名
     */
    public static String generateSign(String apiKey, String secretKey, Long timestamp) {
        // 按照规则拼接：apiKey + secretKey + timestamp
        String data = apiKey + secretKey + timestamp;
        // MD5 加密
        return SecureUtil.md5(data).toUpperCase();
    }

    /**
     * 验证签名
     *
     * @param apiKey    API Key
     * @param secretKey 秘钥
     * @param timestamp 时间戳
     * @param sign      待验证的签名
     * @return 验证结果
     */
    public static boolean checkSign(String apiKey, String secretKey, Long timestamp, String sign) {
        // 1. 检查时间戳是否过期（防止重放攻击）
        long currentTime = System.currentTimeMillis() / 1000;
        long timeDiff = Math.abs(currentTime - timestamp);

        // 默认允许5分钟的时间偏差
        if (timeDiff > 300) {
            return false;
        }

        // 2. 生成签名并比对
        String expectedSign = generateSign(apiKey, secretKey, timestamp);
        return expectedSign.equals(sign);
    }

    /**
     * 生成签名（带参数）
     *
     * @param apiKey    API Key
     * @param secretKey 秘钥
     * @param params    请求参数
     * @return 签名
     */
    public static String generateSignWithParams(String apiKey, String secretKey, Map<String, String> params) {
        // 1. 按字典序排序参数
        TreeMap<String, String> sortedParams = new TreeMap<>(params);

        // 2. 拼接参数字符串
        StringBuilder paramStr = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            paramStr.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }

        // 3. 去掉最后一个 "&"
        if (paramStr.length() > 0) {
            paramStr.deleteCharAt(paramStr.length() - 1);
        }

        // 4. 拼接完整字符串：apiKey + secretKey + params + timestamp
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String data = apiKey + secretKey + paramStr + timestamp;

        // 5. MD5 加密
        return SecureUtil.md5(data).toUpperCase();
    }
}
