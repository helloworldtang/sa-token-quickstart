package com.tangtang.auth.service;

import com.tangtang.auth.entity.ApiKey;
import com.tangtang.auth.entity.User;

/**
 * 认证服务接口
 *
 * @author tangtang
 */
public interface AuthService {

    /**
     * 用户认证
     */
    User authenticateUser(String username, String password);

    /**
     * 获取 API Key 信息
     */
    ApiKey getApiKeyInfo(String apiKey);
}