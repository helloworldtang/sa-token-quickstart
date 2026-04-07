package com.tangtang.satoken.config;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Sa-Token 权限数据接口实现
 * 
 * Sa-Token 会通过此接口获取用户的权限和角色
 * 
 * ⚠️ 安全说明：
 * 实际项目应从数据库或缓存中获取，这里仅作演示
 * 
 * 模拟数据：
 * - 用户 admin (ID: 10001) → admin 角色，所有权限
 * - 用户 user (ID: 10002) → user 角色，只有 user:list 权限
 * 
 * @author 码骨丹心
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    // 模拟用户角色数据（实际应从数据库查询）
    private static final Map<Long, List<String>> USER_ROLES = new HashMap<>();
    
    // 模拟用户权限数据（实际应从数据库查询）
    private static final Map<Long, List<String>> USER_PERMISSIONS = new HashMap<>();
    
    static {
        // admin 用户：拥有 admin 角色和所有权限
        USER_ROLES.put(10001L, Arrays.asList("admin"));
        USER_PERMISSIONS.put(10001L, Arrays.asList("user:list", "user:add", "user:delete"));
        
        // 普通用户：只有 user 角色和 user:list 权限
        USER_ROLES.put(10002L, Arrays.asList("user"));
        USER_PERMISSIONS.put(10002L, Arrays.asList("user:list"));
    }

    /**
     * 获取用户权限列表
     * 
     * 实际项目应从数据库查询：
     * SELECT p.permission_code 
     * FROM user u 
     * JOIN user_role ur ON u.id = ur.user_id 
     * JOIN role_permission rp ON ur.role_id = rp.role_id 
     * JOIN permission p ON rp.permission_id = p.id 
     * WHERE u.id = ?
     * 
     * @param loginId   登录用户ID
     * @param loginType 登录类型
     * @return 权限列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.parseLong(String.valueOf(loginId));
        List<String> permissions = USER_PERMISSIONS.get(userId);
        return permissions != null ? permissions : Collections.emptyList();
    }

    /**
     * 获取用户角色列表
     * 
     * 实际项目应从数据库查询：
     * SELECT r.role_code 
     * FROM user u 
     * JOIN user_role ur ON u.id = ur.user_id 
     * JOIN role r ON ur.role_id = r.id 
     * WHERE u.id = ?
     * 
     * @param loginId   登录用户ID
     * @param loginType 登录类型
     * @return 角色列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.parseLong(String.valueOf(loginId));
        List<String> roles = USER_ROLES.get(userId);
        return roles != null ? roles : Collections.emptyList();
    }
}
