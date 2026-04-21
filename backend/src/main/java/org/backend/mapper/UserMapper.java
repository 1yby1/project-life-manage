package org.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.backend.model.SysUser;

@Mapper
public interface UserMapper extends BaseMapper<SysUser> {
    @Select("SELECT u.*, r.id, r.role_code FROM sys_user u " +
            "LEFT JOIN sys_role r ON u.role_id = r.id " +
            "WHERE u.username = #{username}")
    SysUser findByUsername(String username);
}
