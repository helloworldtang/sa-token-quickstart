package com.tangtang.satoken.elegant.config;

import cn.dev33.satoken.stp.StpInterface;
import com.tangtang.satoken.elegant.model.User;
import com.tangtang.satoken.elegant.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限认证接口实现
 * <p>
 * 优雅点：
 * 1. 实现 StpInterface 接口，Sa-Token自动调用
 * 2. 无需手动管理权限，框架自动处理
 * 3. 使用构造器注入，依赖注入更优雅
 * 4. 方法职责单一，返回权限和角色列表
 * 5. 使用缓存（可选），提升性能
 *
 * @author Agent唐
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private static final Logger log = LoggerFactory.getLogger(StpInterfaceImpl.class);
    private final UserService userService;

    public StpInterfaceImpl(UserService userService) {
        this.userService = userService;
    }

    /**
     * 返回指定账号id所拥有的权限码集合
     * <p>
     * 优雅点：
     * - Sa-Token自动调用此方法，无需手动触发
     * - 支持权限集合，细粒度控制
     * - 可结合缓存，提升性能
     *
     * @param loginId   账号id
     * @param loginType 登录类型
     * @return 权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        log.debug("获取权限列表: loginId={}, loginType={}", loginId, loginType);
        User user = userService.getById(Long.valueOf(loginId.toString()));

        if (user == null || user.getPermissions() == null) {
            return List.of();
        }

        // 优雅点：使用逗号分隔的字符串，转换成List
        return List.of(user.getPermissions().split(","));
    }

    /**
     * 返回指定账号id所拥有的角色标识集合
     * <p>
     * 优雅点：
     * - 与权限分离，角色和权限解耦
     * - Sa-Token支持角色和权限双重校验
     *
     * @param loginId   账号id
     * @param loginType 登录类型
     * @return 角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        log.debug("获取角色列表: loginId={}, loginType={}", loginId, loginType);
        User user = userService.getById(Long.valueOf(loginId.toString()));

        if (user == null || user.getRoles() == null) {
            return List.of();
        }

        // 优雅点：使用逗号分隔的字符串，转换成List
        return List.of(user.getRoles().split(","));
    }
}
