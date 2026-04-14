package com.tangtang.auth.config;

import cn.dev33.satoken.stp.StpInterface;
import com.tangtang.auth.mapper.PermissionMapper;
import com.tangtang.auth.mapper.RoleMapper;
import com.tangtang.auth.mapper.UserMapper;
import com.tangtang.auth.mapper.ApiKeyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限认证接口实现
 * 支持普通用户和 API Key 两种登录类型
 *
 * @author tangtang
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private ApiKeyMapper apiKeyMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId;

        // 根据登录类型判断
        if ("apiKey".equals(loginType)) {
            // API Key 登录：通过 API Key 查询关联的角色权限
            String apiKey = loginId.toString();
            return permissionMapper.selectPermissionsByApiKey(apiKey);
        } else {
            // 普通用户登录：通过用户ID查询权限
            userId = Long.parseLong(loginId.toString());
            return permissionMapper.selectPermissionsByUserId(userId);
        }
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId;

        // 根据登录类型判断
        if ("apiKey".equals(loginType)) {
            // API Key 登录：通过 API Key 查询关联的角色
            String apiKey = loginId.toString();
            return apiKeyMapper.selectRolesByApiKey(apiKey);
        } else {
            // 普通用户登录：通过用户ID查询角色
            userId = Long.parseLong(loginId.toString());
            return roleMapper.selectRolesByUserId(userId);
        }
    }
}
