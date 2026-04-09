package com.tangtang.apisign.exception;

import cn.dev33.satoken.sign.exception.SaSignException;
import com.tangtang.apisign.model.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * <p>签名校验失败时，SaSignUtil 会抛出 {@link SaSignException}，
 * 此处统一拦截并返回友好的错误信息（HTTP 200，业务码 401）。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理签名异常：包含"签名错误"、"时间戳超出允许范围"、"nonce 已被使用"等
     */
    @ExceptionHandler(SaSignException.class)
    public ApiResponse<Void> handleSignException(SaSignException e) {
        log.warn("签名校验失败: {}", e.getMessage());
        return ApiResponse.fail(401, "签名校验失败: " + e.getMessage());
    }

    /**
     * 兜底异常处理
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return ApiResponse.fail(500, "系统异常: " + e.getMessage());
    }
}
