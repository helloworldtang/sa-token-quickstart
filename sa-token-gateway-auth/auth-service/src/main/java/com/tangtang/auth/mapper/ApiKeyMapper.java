package com.tangtang.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tangtang.auth.entity.ApiKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * API Key Mapper
 *
 * @author tangtang
 */
@Mapper
public interface ApiKeyMapper extends BaseMapper<ApiKey> {

    /**
     * 根据 API Key 查询信息
     */
    @Select("SELECT * FROM sys_api_key WHERE api_key = #{apiKey} AND deleted = 0")
    ApiKey selectByApiKey(@Param("apiKey") String apiKey);

    /**
     * 根据 API Key 查询角色
     */
    @Select("SELECT r.role_code FROM sys_role r " +
            "INNER JOIN sys_api_key ak ON r.id = ak.role_id " +
            "WHERE ak.api_key = #{apiKey} AND r.deleted = 0")
    List<String> selectRolesByApiKey(@Param("apiKey") String apiKey);

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