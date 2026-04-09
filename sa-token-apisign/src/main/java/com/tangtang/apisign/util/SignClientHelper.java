package com.tangtang.apisign.util;

import cn.dev33.satoken.sign.template.SaSignUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 签名工具类（客户端调用辅助工具）
 *
 * <p>封装了"客户端如何生成合法签名参数"的完整逻辑，方便测试和前端对接理解：
 * <pre>
 *   1. 收集业务参数
 *   2. 追加 timestamp（毫秒）
 *   3. 追加 nonce（UUID随机串）
 *   4. 按字典序排序所有参数，拼接成 k1=v1&k2=v2&...&key=secretKey
 *   5. MD5 求摘要 → sign
 *   6. 将 timestamp、nonce、sign 一并放入请求参数
 * </pre>
 *
 * <p><b>注意</b>：此类仅供演示"客户端侧生成签名"，生产中客户端调用
 * {@link SaSignUtil#addSignParamsAndJoin(Map)} 即可。
 */
public class SignClientHelper {

    private static final Logger log = Logger.getLogger(SignClientHelper.class.getName());

    /**
     * 为请求参数 Map 追加签名所需字段：timestamp、nonce、sign
     *
     * @param params    原始业务参数（不含 sign/timestamp/nonce，若含则直接使用）
     * @param secretKey 密钥（与服务端配置保持一致）
     * @return 完整参数 Map（含 timestamp/nonce/sign）
     */
    public static Map<String, Object> addSignParams(Map<String, Object> params, String secretKey) {
        Map<String, Object> fullParams = new LinkedHashMap<>(params);

        // 若 params 里没有 timestamp/nonce，自动生成（幂等：有则不覆盖）
        fullParams.putIfAbsent("timestamp", String.valueOf(System.currentTimeMillis()));
        fullParams.putIfAbsent("nonce", UUID.randomUUID().toString().replace("-", ""));

        // 按字典序排列，拼接 key1=val1&key2=val2&...&key=secretKey
        Map<String, Object> sorted = new TreeMap<>(fullParams);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : sorted.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        sb.append("key=").append(secretKey);

        String rawStr = sb.toString();
        log.fine("待签名字符串: " + rawStr);

        String sign = md5(rawStr);
        fullParams.put("sign", sign);

        log.fine("生成签名: " + sign);
        return fullParams;
    }

    /**
     * MD5 摘要（小写 32 位十六进制）
     */
    public static String md5(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 计算失败", e);
        }
    }

    /**
     * 将 Map 参数拼接成 URL query string（k=v&k=v 格式）
     */
    public static String toQueryString(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        params.forEach((k, v) -> {
            if (!sb.isEmpty()) {
                sb.append("&");
            }
            sb.append(k).append("=").append(v);
        });
        return sb.toString();
    }
}
