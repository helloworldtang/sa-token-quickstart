package com.tangtang.satoken.elegant.service.impl;

import com.tangtang.satoken.elegant.exception.BusinessException;
import com.tangtang.satoken.elegant.model.User;
import com.tangtang.satoken.elegant.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户服务实现类
 * <p>
 * 优雅点：
 * 1. 使用内存存储（演示用），生产环境可替换为数据库
 * 2. 使用ConcurrentHashMap，线程安全
 * 3. 构造函数中初始化测试数据
 * 4. 方法职责单一，逻辑清晰
 *
 * @author Agent唐
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    /**
     * 模拟数据库存储
     * 优雅点：使用ConcurrentHashMap，线程安全，适合高并发场景
     */
    private final Map<String, User> userMap = new ConcurrentHashMap<>();

    /**
     * 构造函数，初始化测试数据
     * <p>
     * 优雅点：在Bean创建时自动初始化数据
     */
    public UserServiceImpl() {
        // 初始化测试用户
        User admin = User.builder()
                .id(1L)
                .username("admin")
                .password("123456")  // 实际项目应加密存储
                .nickname("管理员")
                .roles("admin")
                .permissions("user:add,user:edit,user:delete,user:view")
                .build();

        User user = User.builder()
                .id(2L)
                .username("user")
                .password("123456")  // 实际项目应加密存储
                .nickname("普通用户")
                .roles("user")
                .permissions("user:view")
                .build();

        userMap.put(admin.getUsername(), admin);
        userMap.put(user.getUsername(), user);

        log.info("初始化用户数据完成，共{}个用户", userMap.size());
    }

    /**
     * 根据用户名获取用户
     */
    @Override
    public User getByUsername(String username) {
        return userMap.get(username);
    }

    /**
     * 根据ID获取用户
     */
    @Override
    public User getById(Long id) {
        return userMap.values().stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * 校验密码
     * 优雅点：实际项目中应使用BCrypt等加密算法
     */
    @Override
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        // 演示项目直接比较，实际项目应使用BCryptPasswordEncoder
        return rawPassword.equals(encodedPassword);
    }

    /**
     * 校验用户是否存在
     * <p>
     * 优雅点：抛出业务异常，错误信息明确
     */
    public User validateUser(String username, String password) {
        User user = getByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!checkPassword(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        return user;
    }
}
