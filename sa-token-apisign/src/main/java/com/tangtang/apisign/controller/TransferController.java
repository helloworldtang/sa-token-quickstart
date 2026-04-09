package com.tangtang.apisign.controller;

import cn.dev33.satoken.sign.template.SaSignUtil;
import cn.dev33.satoken.context.SaHolder;
import com.tangtang.apisign.model.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 模拟"转账"业务接口
 *
 * <p>演示签名校验的最佳实践：手动调用 {@link SaSignUtil#checkRequest} 校验签名。
 *
 * <p>客户端请求时需携带以下额外参数：
 * <pre>
 *   timestamp  - 当前毫秒时间戳，误差不超过 15 分钟
 *   nonce      - 随机字符串，每次请求不同（防重放）
 *   sign       - 对所有参数（含密钥）按字典序排序后 MD5 的值
 * </pre>
 */
@Tag(name = "转账业务", description = "模拟转账业务接口，演示签名校验的使用")
@RestController
@RequestMapping("/api/transfer")
public class TransferController {

    private static final Logger log = LoggerFactory.getLogger(TransferController.class);

    /**
     * 发起转账
     *
     * <p>完整请求示例（先访问 /sign/help 获取带签名的 URL）：
     * <pre>
     * GET /api/transfer/submit?userId=1001&amp;amount=5000&amp;remark=测试
     *     &amp;timestamp=xxx&amp;nonce=xxx&amp;sign=xxx
     * </pre>
     */
    @Operation(summary = "发起转账", description = "发起转账请求（需要签名校验）。提示：可先访问 /sign/help 获取带签名的 URL")
    @GetMapping("/submit")
    public ApiResponse<Map<String, Object>> submitTransfer(
            @Parameter(description = "用户ID", required = true, example = "1001") @RequestParam Long userId,
            @Parameter(description = "转账金额", required = true, example = "5000") @RequestParam Long amount,
            @Parameter(description = "转账备注", example = "测试") @RequestParam(required = false, defaultValue = "") String remark) {

        // ★ 核心：一行代码完成签名校验（校验失败自动抛出 SaSignException）
        SaSignUtil.checkRequest(SaHolder.getRequest());

        log.info("签名校验通过，处理转账: userId={}, amount={}, remark={}", userId, amount, remark);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", "TF" + System.currentTimeMillis());
        result.put("userId", userId);
        result.put("amount", amount);
        result.put("remark", remark);
        result.put("status", "SUCCESS");

        return ApiResponse.ok("转账成功", result);
    }

    /**
     * 查询账户余额
     *
     * <p>注意：即使是"查询"接口，也应当签名，防止参数篡改（例如篡改 userId 查他人余额）
     */
    @Operation(summary = "查询余额", description = "查询账户余额（需要签名校验）。即使是查询接口也应签名，防止参数篡改")
    @GetMapping("/balance")
    public ApiResponse<Map<String, Object>> queryBalance(
            @Parameter(description = "用户ID", required = true, example = "1001") @RequestParam Long userId) {

        SaSignUtil.checkRequest(SaHolder.getRequest());

        log.info("签名校验通过，查询余额: userId={}", userId);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("balance", 99999L);  // 模拟数据
        result.put("currency", "CNY");

        return ApiResponse.ok(result);
    }

    /**
     * 生成演示签名参数（仅供测试理解用）
     *
     * <p>访问此接口可获得一个合法的带签名 URL，直接拷贝到浏览器即可验证。
     * <b>生产环境不应暴露此类接口！</b>
     */
    @Operation(summary = "生成演示签名", description = "生成演示签名参数，可获得一个合法的带签名 URL（仅供测试理解用，生产环境不应暴露）")
    @GetMapping("/demo-sign")
    public ApiResponse<Map<String, Object>> generateDemoSign(
            @Parameter(description = "用户ID", example = "1001") @RequestParam(defaultValue = "1001") Long userId,
            @Parameter(description = "转账金额", example = "5000") @RequestParam(defaultValue = "5000") Long amount) {

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("amount", amount);
        params.put("remark", "测试转账");

        // SaSignUtil 自动追加 timestamp、nonce、sign
        String fullQueryString = SaSignUtil.addSignParamsAndJoin(params);

        Map<String, Object> result = new HashMap<>();
        result.put("queryString", fullQueryString);
        result.put("tip", "将此 queryString 拼接到 /api/transfer/submit 后即可请求");
        result.put("exampleUrl", "http://localhost:8083/api/transfer/submit?" + fullQueryString);

        return ApiResponse.ok("签名参数已生成", result);
    }
}
