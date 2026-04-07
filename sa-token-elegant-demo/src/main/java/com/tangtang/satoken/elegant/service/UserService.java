package com.tangtang.satoken.elegant.service;

import com.tangtang.satoken.elegant.model.User;

/**
 * 用户服务接口
 * <p>
 * 优雅点：
 * 1. 面向接口编程，解耦具体实现
 * 2. 接口定义清晰，职责单一
 *
 * @author Agent唐
 */
public interface UserService {

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getByUsername(String username);

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    User getById(Long id);

    /**
     * 校验密码
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    boolean checkPassword(String rawPassword, String encodedPassword);
}
