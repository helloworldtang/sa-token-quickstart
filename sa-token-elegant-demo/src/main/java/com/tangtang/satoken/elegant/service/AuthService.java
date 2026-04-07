package com.tangtang.satoken.elegant.service;

import com.tangtang.satoken.elegant.model.dto.KickoutRequest;
import com.tangtang.satoken.elegant.model.dto.LoginRequest;
import com.tangtang.satoken.elegant.model.dto.LoginResponse;
import com.tangtang.satoken.elegant.model.dto.OnlineSessionDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 认证服务接口
 * <p>
 * 优雅点：
 * 1. 认证相关操作集中管理
 * 2. 接口定义清晰，职责单一
 *
 * @author Agent唐
 */
public interface AuthService {

    /**
     * 用户登录
     * <p>
     * 优雅点：
     * - 返回LoginResponse，包含Token和用户信息
     * - 一次登录返回所有必要信息，减少前端请求
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户登出
     * <p>
     * 优雅点：
     * - Sa-Token一行代码实现登出
     * - 自动清除Token
     */
    void logout();

    /**
     * 获取当前登录用户信息
     *
     * @return 登录响应
     */
    LoginResponse getCurrentUser();

    /**
     * 踢人下线
     * <p>
     * 优雅点：
     * - 指定Token踢人，精确控制
     * - 支持踢掉所有登录会话
     *
     * @param token Token值
     */
    void kickout(String token);

    /**
     * 踢人下线（根据用户ID）
     * <p>
     * 优雅点：
     * - 根据用户ID踢掉所有会话
     * - 符合后台管理习惯
     * - 无需知道用户Token
     *
     * @param userId 用户ID
     * @param request 踢人请求
     */
    void kickoutByUserId(Long userId, KickoutRequest request);

    /**
     * 踢掉指定会话
     * <p>
     * 优雅点：
     * - 根据会话ID踢掉单个会话
     * - 精细化控制
     * - 支持多设备登录场景
     *
     * @param sessionId 会话ID
     * @param request 踢人请求
     */
    void kickoutBySessionId(String sessionId, KickoutRequest request);

    /**
     * 获取在线会话列表
     * <p>
     * 优雅点：
     * - 返回在线用户的会话信息
     * - 不包含敏感Token
     * - 支持会话管理
     *
     * @return 在线会话列表
     */
    List<OnlineSessionDTO> getOnlineSessions();
}
