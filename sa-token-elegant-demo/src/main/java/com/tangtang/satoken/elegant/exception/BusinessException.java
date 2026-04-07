package com.tangtang.satoken.elegant.exception;

import lombok.Getter;

/**
 * 业务异常
 * <p>
 * 优雅点：
 * 1. 继承RuntimeException，无需显式抛出
 * 2. 包含错误码和错误信息，灵活控制响应
 * 3. 使用Lombok的@Getter，自动生成getter方法
 *
 * @author Agent唐
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;

    /**
     * 构造函数（仅错误信息）
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    /**
     * 构造函数（错误码 + 错误信息）
     * <p>
     * 优雅点：重载构造函数，提供多种创建方式
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
