package com.tangtang.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tangtang.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户 Mapper
 *
 * @author tangtang
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据用户ID查询权限
     */
    @Select("SELECT p.permission_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_role r ON rp.role_id = r.id " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = 0")
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);
}