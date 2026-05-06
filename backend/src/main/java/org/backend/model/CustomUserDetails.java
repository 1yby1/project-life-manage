package org.backend.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.List;

/**
 * 自定义 Spring Security UserDetails
 * - 暴露 userId / realName 给业务代码
 * - 持有当前用户的全部角色 code (从 sys_user_role 加载)
 * - roleId 字段保留用于向后兼容旧代码
 */
@Getter
public class CustomUserDetails extends User {
    private final Long userId;
    /** 主角色 ID(兼容旧代码,实际权限以 roleCodes 为准) */
    private final Long roleId;
    private final String realName;
    /** 全部角色编码,如 ["ADMIN","USER"] */
    private final List<String> roleCodes;

    public CustomUserDetails(SysUser sysUser,
                             List<String> roleCodes,
                             Collection<? extends GrantedAuthority> authorities) {
        super(sysUser.getUsername(), sysUser.getPassword(), authorities);
        this.userId = sysUser.getId();
        this.roleId = sysUser.getRoleId();
        this.realName = sysUser.getRealName();
        this.roleCodes = roleCodes;
    }
}
