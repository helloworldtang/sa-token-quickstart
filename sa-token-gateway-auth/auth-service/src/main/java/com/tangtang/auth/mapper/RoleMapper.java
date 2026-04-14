package com.tangtang.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tangtang.auth.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色 Mapper
 *
 * @author tangtang
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户ID查询角色
     */
    @Select("SELECT r.role_code FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<String> selectRolesByUserId(@Param("userId") Long userId);
}