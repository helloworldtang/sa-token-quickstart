package com.tangtang.satoken.elegant.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 在线会话DTO
 * <p>
 * 优雅点：
 * 1. 不包含敏感的Token信息
 * 2. 提供完整的会话上下文
 * 3. 支持精细化的会话管理
 *
 * @author Agent唐
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "在线会话信息")
public class OnlineSessionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID（不含敏感Token）
     */
    @Schema(description = "会话ID")
    private String sessionId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    private String nickname;

    /**
     * 角色
     */
    @Schema(description = "角色")
    private String roles;

    /**
     * 登录时间
     */
    @Schema(description = "登录时间")
    private LocalDateTime loginTime;

    /**
     * 最后活跃时间
     */
    @Schema(description = "最后活跃时间")
    private LocalDateTime lastActiveTime;

    /**
     * IP地址
     */
    @Schema(description = "IP地址")
    private String ip;

    /**
     * 设备信息
     */
    @Schema(description = "设备信息")
    private String device;
}