package com.tangtang.satoken.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.util.SaResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 
 * 统一处理 Sa-Token 异常，返回标准 JSON 格式
 * 
 * @author 码骨丹心
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public SaResult handleNotLoginException(NotLoginException e) {
        return SaResult
                .code(401)
                .setMsg("请先登录");
    }

    /**
     * 缺少角色异常
     */
    @ExceptionHandler(NotRoleException.class)
    public SaResult handleNotRoleException(NotRoleException e) {
        return SaResult
                .code(403)
                .setMsg("缺少角色: " + e.getRole());
    }

    /**
     * 缺少权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public SaResult handleNotPermissionException(NotPermissionException e) {
        return SaResult
                .code(403)
                .setMsg("缺少权限: " + e.getPermission());
    }

    /**
     * 其他异常
     */
    @ExceptionHandler(Exception.class)
    public SaResult handleException(Exception e) {
        return SaResult
                .code(500)
                .setMsg("系统异常: " + e.getMessage());
    }
}
