package com.tangtang.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tangtang.auth.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限 Mapper
 *
 * @author tangtang
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据用户ID查询权限
     */
    @Select("SELECT p.permission_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_role r ON rp.role_id = r.id " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = 0")
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 根据 API Key 查询权限
     */
    @Select("SELECT p.permission_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_role r ON rp.role_id = r.id " +
            "INNER JOIN sys_api_key ak ON r.id = ak.role_id " +
            "WHERE ak.api_key = #{apiKey} AND p.deleted = 0")
    List<String> selectPermissionsByApiKey(@Param("apiKey") String apiKey);
}