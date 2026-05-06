package org.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.backend.model.SysUser;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<SysUser> {
    @Select("SELECT u.*, r.id, r.role_code FROM sys_user u " +
            "LEFT JOIN sys_role r ON u.role_id = r.id " +
            "WHERE u.username = #{username}")
    SysUser findByUsername(String username);

    /**
     * 查找拥有指定角色 code 且账号有效的用户
     * 用于 OPP_ADMIN 派单时选择 CUSTOMER_MANAGER 等场景
     */
    @Select("SELECT u.* FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON ur.user_id = u.id " +
            "INNER JOIN sys_role r ON r.id = ur.role_id " +
            "WHERE r.role_code = #{roleCode} AND u.status = 1 " +
            "ORDER BY u.id")
    List<SysUser> findByRoleCode(@Param("roleCode") String roleCode);
}

