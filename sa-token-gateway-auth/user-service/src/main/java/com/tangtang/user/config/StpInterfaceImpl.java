package com.tangtang.user.config;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限认证接口实现
 *
 * @author tangtang
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 模拟从数据库查询用户权限
        Long userId = Long.parseLong(loginId.toString());

        if (userId == 10001L) {
            // admin 用户拥有所有权限
            List<String> permissions = new ArrayList<>();
            permissions.add("user:list");
            permissions.add("user:add");
            permissions.add("user:delete");
            permissions.add("order:list");
            permissions.add("order:create");
            return permissions;
        } else if (userId == 10002L) {
            // user 用户只有部分权限
            List<String> permissions = new ArrayList<>();
            permissions.add("user:list");
            permissions.add("order:list");
            return permissions;
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 模拟从数据库查询用户角色
        Long userId = Long.parseLong(loginId.toString());

        if (userId == 10001L) {
            List<String> roles = new ArrayList<>();
            roles.add("admin");
            return roles;
        } else if (userId == 10002L) {
            List<String> roles = new ArrayList<>();
            roles.add("user");
            return roles;
        }
        return new ArrayList<>();
    }
}
