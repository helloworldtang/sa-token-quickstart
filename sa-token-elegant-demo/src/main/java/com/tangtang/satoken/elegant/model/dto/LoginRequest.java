package com.tangtang.satoken.elegant.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录请求DTO
 * <p>
 * 优雅点：
 * 1. 使用DTO对象，参数校验更优雅
 * 2. 使用JSR-303校验注解，参数验证自动完成
 * 3. 使用Swagger注解，API文档自动生成
 * 4. 使用Lombok注解，减少样板代码
 *
 * @author Agent唐
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录请求")
public class LoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "admin")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;
}
