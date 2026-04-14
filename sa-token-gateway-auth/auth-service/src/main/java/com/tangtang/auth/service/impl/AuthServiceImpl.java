package com.tangtang.auth.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.tangtang.auth.entity.ApiKey;
import com.tangtang.auth.entity.User;
import com.tangtang.auth.mapper.ApiKeyMapper;
import com.tangtang.auth.mapper.UserMapper;
import com.tangtang.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现
 *
 * @author tangtang
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ApiKeyMapper apiKeyMapper;

    @Override
    public User authenticateUser(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    @Override
    public ApiKey getApiKeyInfo(String apiKey) {
        return apiKeyMapper.selectByApiKey(apiKey);
    }
}