package com.tangtang.auth.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.util.SaResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author tangtang
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public SaResult handleNotLoginException(NotLoginException e) {
        return SaResult.error("请先登录").setCode(401);
    }

    /**
     * 处理无权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public SaResult handleNotPermissionException(NotPermissionException e) {
        return SaResult.error("无权限访问").setCode(403);
    }

    /**
     * 处理无角色异常
     */
    @ExceptionHandler(NotRoleException.class)
    public SaResult handleNotRoleException(NotRoleException e) {
        return SaResult.error("无角色访问").setCode(403);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public SaResult handleException(Exception e) {
        e.printStackTrace();
        return SaResult.error("系统异常：" + e.getMessage()).setCode(500);
    }
}