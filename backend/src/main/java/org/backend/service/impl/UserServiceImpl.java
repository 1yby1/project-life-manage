package org.backend.service.impl;

import org.backend.mapper.UserMapper;
import org.backend.model.SysUser;
import org.backend.model.Dto.RegisterRequest;
import org.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public String register(RegisterRequest request) {
        // 1. 检查用户名是否已存在
        SysUser existingUser = userMapper.findByUsername(request.getUsername());
        if (existingUser != null) {
            return "用户名已存在";
        }

        // 2. 创建新用户对象
        SysUser newUser = new SysUser();
        newUser.setUsername(request.getUsername());
        // 注意：密码一定要使用 Spring Security 提供的 PasswordEncoder 加密存储
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRealName(request.getRealName());

        // 默认给定一个角色ID，比如 2L (需要确保你的 sys_role 表里有 id=2 的数据)，或者根据具体业务逻辑修改
        newUser.setRoleId(2L);
        newUser.setStatus((byte) 1); // 1: 正常
        newUser.setCreateTime(LocalDateTime.now());

        // 3. 写入数据库 (Mybatis-Plus 的 insert 方法)
        int result = userMapper.insert(newUser);
        if (result > 0) {
            return null; // 成功返回 null 作为无错误的标志
        } else {
            return "注册失败，请稍后再试";
        }
    }
}

