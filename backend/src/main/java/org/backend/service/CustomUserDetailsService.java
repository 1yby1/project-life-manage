package org.backend.service;

import org.backend.mapper.UserMapper;
import org.backend.mapper.UserRoleMapper;
import org.backend.model.CustomUserDetails;
import org.backend.model.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = userMapper.findByUsername(username);
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        if (sysUser.getStatus() == 0) {
            throw new UsernameNotFoundException("该账号已被禁用");
        }

        // 多角色: 从 sys_user_role + sys_role 加载所有 role_code
        List<String> roleCodes = userRoleMapper.selectRoleCodesByUserId(sysUser.getId());
        if (roleCodes == null) {
            roleCodes = new ArrayList<>();
        } else {
            // 防御性拷贝: 后续可能 add USER 兜底
            roleCodes = new ArrayList<>(roleCodes);
        }
        // 兜底: 任何用户都至少有 USER 角色,与需求文档一致
        if (!roleCodes.contains("USER")) {
            roleCodes.add("USER");
        }

        // Spring Security 的 hasRole('XXX') 默认要求 authority 形如 "ROLE_XXX"
        List<GrantedAuthority> authorities = roleCodes.stream()
                .map(code -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + code))
                .collect(Collectors.toList());

        return new CustomUserDetails(sysUser, Collections.unmodifiableList(roleCodes), authorities);
    }
}
