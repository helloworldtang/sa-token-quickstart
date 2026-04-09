package com.tangtang.apisign.controller;

import com.tangtang.apisign.model.ApiResponse;
import com.tangtang.apisign.util.SignClientHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 签名演示控制器：帮助理解"客户端如何生成签名"
 *
 * <p>直接在浏览器打开 <code>/sign/help</code> 即可看到如何构造一个合法请求。
 */
@Tag(name = "签名演示", description = "帮助理解客户端如何生成签名的演示接口")
@RestController
@RequestMapping("/sign")
public class SignDemoController {

    private static final Logger log = LoggerFactory.getLogger(SignDemoController.class);

    @Value("${sa-token.sign.secret-key}")
    private String secretKey;

    /**
     * 签名帮助接口：给定业务参数，分步展示签名生成过程，并返回完整的可用 URL
     */
    @Operation(summary = "签名生成帮助", description = "给定业务参数，分步展示签名生成过程，并返回完整的可用 URL")
    @GetMapping("/help")
    public ApiResponse<Map<String, Object>> signHelp(
            @Parameter(description = "用户ID", example = "1001") @RequestParam(defaultValue = "1001") Long userId,
            @Parameter(description = "金额", example = "5000") @RequestParam(defaultValue = "5000") Long amount,
            @Parameter(description = "备注", example = "测试") @RequestParam(defaultValue = "测试") String remark) {

        Map<String, Object> bizParams = new HashMap<>();
        bizParams.put("userId", userId);
        bizParams.put("amount", amount);
        bizParams.put("remark", remark);

        // 使用我们自己实现的辅助类，演示签名生成步骤
        Map<String, Object> signedParams = SignClientHelper.addSignParams(bizParams, secretKey);
        String queryString = SignClientHelper.toQueryString(signedParams);

        Map<String, Object> result = new HashMap<>();
        result.put("step1_bizParams", bizParams);
        result.put("step2_signedParams", signedParams);
        result.put("step3_queryString", queryString);
        result.put("step4_fullUrl", "http://localhost:8083/api/transfer/submit?" + queryString);
        result.put("tip", "复制 step4_fullUrl 到浏览器/Postman 即可发起合法请求（15分钟内有效）");

        return ApiResponse.ok("签名示例生成成功", result);
    }
}
