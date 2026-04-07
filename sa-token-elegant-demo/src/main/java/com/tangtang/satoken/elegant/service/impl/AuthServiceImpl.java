package com.tangtang.satoken.elegant.service.impl;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.stp.StpUtil;
import com.tangtang.satoken.elegant.model.User;
import com.tangtang.satoken.elegant.model.dto.KickoutRequest;
import com.tangtang.satoken.elegant.model.dto.LoginRequest;
import com.tangtang.satoken.elegant.model.dto.LoginResponse;
import com.tangtang.satoken.elegant.model.dto.OnlineSessionDTO;
import com.tangtang.satoken.elegant.service.AuthService;
import com.tangtang.satoken.elegant.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 认证服务实现类
 * <p>
 * 优雅点：
 * 1. 使用Sa-Token的StpUtil，一行代码完成复杂操作
 * 2. API设计简洁，语义清晰
 * 3. 无需手动管理Token，框架自动处理
 * 4. 支持多种Token操作（登录、登出、踢人等）
 *
 * @author Agent唐
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserService userService;

    @Value("${sa-token.token-name}")
    private String tokenName;

    public AuthServiceImpl(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户登录
     * <p>
     * 优雅点展示：
     * 1. StpUtil.login(id) - 一行代码完成登录，自动生成Token
     * 2. StpUtil.getTokenValue() - 获取当前Token
     * 3. StpUtil.getSession() - 获取Session，可存储自定义数据
     * 4. 无需手动管理Token生命周期
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录: {}", request.getUsername());

        // 1. 校验用户名和密码
        User user = ((UserServiceImpl) userService).validateUser(
                request.getUsername(),
                request.getPassword()
        );

        // 2. Sa-Token登录 - 优雅点：一行代码完成登录
        //    会自动：生成Token、创建Session、存储登录状态
        StpUtil.login(user.getId());

        // 3. 将用户信息存入Session - 优雅点：Session自动绑定到Token
        StpUtil.getSession().set("user", user);

        // 4. 构建响应
        return new LoginResponse(
                StpUtil.getTokenValue(),
                tokenName,
                user.getId(),
                user.getUsername(),
                user.getNickname()
        );
    }

    /**
     * 用户登出
     * <p>
     * 优雅点展示：
     * 1. StpUtil.logout() - 一行代码完成登出
     * 2. 自动清除Token、Session
     * 3. 无需手动清理状态
     */
    @Override
    public void logout() {
        log.info("用户登出: {}", StpUtil.getLoginIdDefaultNull());
        StpUtil.logout();
    }

    /**
     * 获取当前登录用户信息
     * <p>
     * 优雅点展示：
     * 1. StpUtil.isLogin() - 判断是否登录
     * 2. StpUtil.getLoginId() - 获取登录ID
     * 3. StpUtil.getSession() - 获取Session数据
     * 4. 所有操作都经过框架校验，安全可靠
     *
     * @return 登录响应
     */
    @Override
    public LoginResponse getCurrentUser() {
        // 优雅点：Sa-Token自动校验登录状态，未登录会抛出NotLoginException
        Long userId = StpUtil.getLoginIdAsLong();

        // 从Session获取用户信息
        User user = (User) StpUtil.getSession().get("user");

        return new LoginResponse(
                StpUtil.getTokenValue(),
                tokenName,
                user.getId(),
                user.getUsername(),
                user.getNickname()
        );
    }

    /**
     * 踢人下线
     * <p>
     * 优雅点展示：
     * 1. StpUtil.kickout(tokenValue) - 根据Token踢人
     * 2. StpUtil.logout(userId) - 根据用户ID踢掉该用户所有会话
     * 3. 支持精确控制，灵活方便
     *
     * @param token Token值
     */
    @Override
    public void kickout(String token) {
        log.info("踢人下线: token={}", token);
        StpUtil.kickoutByTokenValue(token);
    }

    /**
     * 根据用户ID踢人
     * <p>
     * 优雅点展示：
     * 1. 根据用户ID踢掉所有会话
     * 2. 符合后台管理习惯
     * 3. 无需知道用户Token
     *
     * @param userId  用户ID
     * @param request 踢人请求
     */
    @Override
    public void kickoutByUserId(Long userId, KickoutRequest request) {
        String reason = request != null && request.getReason() != null
                ? request.getReason()
                : "管理员操作";

        log.info("管理员踢人: userId={}, reason={}", userId, reason);
        // 根据用户ID踢掉该用户的所有会话
        StpUtil.kickout(userId);
    }

    /**
     * 踢掉指定会话
     * <p>
     * 优雅点展示：
     * 1. 根据会话ID踢掉单个会话
     * 2. 精细化控制
     * 3. 支持多设备登录场景
     *
     * @param sessionId 会话ID
     * @param request   踢人请求
     */
    @Override
    public void kickoutBySessionId(String sessionId, KickoutRequest request) {
        String reason = request != null && request.getReason() != null
                ? request.getReason()
                : "管理员操作";

        log.info("管理员踢掉会话: sessionId={}, reason={}", sessionId, reason);

        // 根据会话ID获取Token，然后踢人
        String token = getTokenBySessionId(sessionId);
        if (token == null) {
            log.warn("会话不存在: sessionId={}", sessionId);
            return;
        }

        StpUtil.kickoutByTokenValue(token);
    }

    /**
     * 获取在线会话列表
     * <p>
     * 优雅点展示：
     * 1. 返回在线用户的会话信息
     * 2. 不包含敏感Token
     * 3. 支持会话管理
     *
     * @return 在线会话列表
     */
    @Override
    public List<OnlineSessionDTO> getOnlineSessions() {
        log.info("获取在线会话列表");

        // 获取所有token（搜索null，获取所有登录的token）
        List<String> tokens = StpUtil.searchTokenValue("", 0, -1, false);

        // 构建会话列表（不包含敏感Token）
        LocalDateTime now = LocalDateTime.now();
        List<OnlineSessionDTO> sessions = tokens.stream()
                .map(token -> {
                    try {
                        // 获取 token 前缀（如 satoken:login:token:）
                        String tokenPrefix = SaManager.getConfig().getTokenName() + ":" + StpUtil.getLoginType() + ":token:";
                        // 从完整 Key 中移除前缀，得到纯 token
                        String tokenValue = token.replaceFirst(tokenPrefix, "");
                        // 获取登录ID
                        Object loginId = StpUtil.getLoginIdByToken(tokenValue);
                        if (loginId == null) {
                            return null;
                        }

                        // 获取用户信息
                        Long userId = Long.valueOf(loginId.toString());
                        User user = userService.getById(userId);
                        if (user == null) {
                            return null;
                        }

                        // 构建会话信息（不含敏感Token）
                        return new OnlineSessionDTO(
                                loginId.toString(),  // sessionId就是loginId
                                userId,
                                user.getUsername(),
                                user.getNickname(),
                                user.getRoles(),
                                now,
                                now,
                                "127.0.0.1",
                                "Chrome / macOS"
                        );
                    } catch (Exception e) {
                        log.warn("处理会话失败: token={}", token, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("获取在线会话列表: 共{}个会话", sessions.size());
        return sessions;
    }

    /**
     * 根据会话ID获取Token
     * <p>
     * 优雅点：通过搜索方式获取Token
     * <p>
     * 注意：在Sa-Token中，sessionId实际上就是loginId（登录ID）
     * 因此sessionId的值就是用户的ID（如：1, 2, 3...）
     *
     * @param sessionId 会话ID（实际上是用户ID）
     * @return Token值
     */
    private String getTokenBySessionId(String sessionId) {
        try {
            // 获取所有token（搜索null，获取所有登录的token）
            List<String> tokens = StpUtil.searchTokenValue(null, 0, -1, false);

            // 遍历所有token，找到匹配sessionId的那个
            for (String token : tokens) {
                try {
                    Object loginId = StpUtil.getLoginIdByToken(token);
                    if (loginId != null && loginId.toString().equals(sessionId)) {
                        return token;
                    }
                } catch (Exception e) {
                    // token可能已失效，继续检查下一个
                }
            }

            log.warn("未找到匹配的Token: sessionId={}", sessionId);
            return null;
        } catch (Exception e) {
            log.error("获取Token失败: sessionId={}", sessionId, e);
            return null;
        }
    }

}