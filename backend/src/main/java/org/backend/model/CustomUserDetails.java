package org.backend.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {
    private final Long userId;
    private final Long roleId;
    private final String realName;

    public CustomUserDetails(SysUser sysUser, Collection<? extends GrantedAuthority> authorities) {
        super(sysUser.getUsername(), sysUser.getPassword(), authorities);
        this.userId = sysUser.getId();
        this.roleId = sysUser.getRoleId();
        this.realName = sysUser.getRealName();
    }
}

