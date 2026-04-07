package com.tangtang.satoken.elegant.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.tangtang.satoken.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * <p>
 * 优雅点：
 * 1. 使用 @RestControllerAdvice，统一处理所有异常
 * 2. 返回统一的 Result 格式，前端处理更优雅
 * 3. 分类处理不同异常，错误信息清晰
 * 4. 使用Lombok的@Slf4j，日志记录方便
 *
 * @author Agent唐
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理未登录异常
     * <p>
     * 优雅点：Sa-Token提供专门的异常类型，捕获更精确
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLoginException(NotLoginException e) {
        log.warn("未登录异常: {}", e.getMessage());
        String message = switch (e.getType()) {
            case NotLoginException.NOT_TOKEN -> "未提供Token";
            case NotLoginException.INVALID_TOKEN -> "Token无效";
            case NotLoginException.TOKEN_TIMEOUT -> "Token已过期";
            case NotLoginException.BE_REPLACED -> "Token已被替换";
            case NotLoginException.KICK_OUT -> "Token已被踢出";
            default -> "未登录";
        };
        return Result.error(401, message);
    }

    /**
     * 处理无权限异常
     * <p>
     * 优雅点：Sa-Token提供权限级异常，细粒度控制
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<?> handleNotPermissionException(NotPermissionException e) {
        log.warn("无权限异常: {}", e.getMessage());
        return Result.error(403, "权限不足: " + e.getPermission());
    }

    /**
     * 处理无角色异常
     * <p>
     * 优雅点：Sa-Token提供角色级异常，细粒度控制
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<?> handleNotRoleException(NotRoleException e) {
        log.warn("无角色异常: {}", e.getMessage());
        return Result.error(403, "角色不足: " + e.getRole());
    }

    /**
     * 处理参数校验异常
     * <p>
     * 优雅点：自动提取校验失败的字段和错误信息
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<?> handleValidationException(Exception e) {
        log.warn("参数校验异常: {}", e.getMessage());
        String errorMessage = "参数校验失败";

        if (e instanceof MethodArgumentNotValidException ex) {
            FieldError fieldError = ex.getBindingResult().getFieldError();
            if (fieldError != null) {
                errorMessage = fieldError.getField() + ": " + fieldError.getDefaultMessage();
            }
        } else if (e instanceof BindException ex) {
            FieldError fieldError = ex.getBindingResult().getFieldError();
            if (fieldError != null) {
                errorMessage = fieldError.getField() + ": " + fieldError.getDefaultMessage();
            }
        }

        return Result.error(400, errorMessage);
    }

    /**
     * 处理其他异常
     * <p>
     * 优雅点：兜底处理，避免异常信息泄露
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error("系统异常，请稍后重试");
    }
}
