package com.tangtang.satoken.elegant.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录响应DTO
 * <p>
 * 优雅点：
 * 1. 返回结构化的数据，前端使用更方便
 * 2. 包含Token和用户信息，减少前端请求次数
 * 3. 使用Swagger注解，API文档清晰
 *
 * @author Agent唐
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Token值
     */
    @Schema(description = "Token值")
    private String token;

    /**
     * Token名称
     */
    @Schema(description = "Token名称")
    private String tokenName;

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
}
