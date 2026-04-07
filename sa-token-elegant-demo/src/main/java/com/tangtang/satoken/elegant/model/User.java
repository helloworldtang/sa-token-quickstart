package com.tangtang.satoken.elegant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户实体
 * <p>
 * 优雅点：
 * 1. 使用Lombok注解，减少样板代码
 * 2. 字段注释清晰
 * 3. 使用Builder模式，对象构建更优雅
 *
 * @author Agent唐
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（实际项目中应加密存储）
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 角色（逗号分隔）
     * 优雅点：使用字符串存储多个角色，简化数据结构
     */
    private String roles;

    /**
     * 权限标识（逗号分隔）
     * 优雅点：使用字符串存储多个权限，简化数据结构
     */
    private String permissions;
}
