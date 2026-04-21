package org.backend.service;

import org.backend.mapper.UserMapper;
import org.backend.model.CustomUserDetails;
import org.backend.model.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = userMapper.findByUsername(username);
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        if (sysUser.getStatus() == 0) {
            throw new UsernameNotFoundException("该账号已被禁用");
        }

        // 返回自定义的 CustomUserDetails，携带 userId, roleId 等信息
        return new CustomUserDetails(
                sysUser,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + sysUser.getRoleId()))
        );
    }
}
