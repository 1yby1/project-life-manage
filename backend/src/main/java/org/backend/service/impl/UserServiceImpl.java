package org.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.backend.mapper.RoleMapper;
import org.backend.mapper.UserMapper;
import org.backend.model.Dto.AdminUserCreateRequest;
import org.backend.model.Dto.AdminUserUpdateRequest;
import org.backend.model.Dto.PageResult;
import org.backend.model.Dto.RegisterRequest;
import org.backend.model.Dto.UserListItemDto;
import org.backend.model.SysRole;
import org.backend.model.SysUser;
import org.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public String register(RegisterRequest request) {
        SysUser existing = userMapper.findByUsername(request.getUsername());
        if (existing != null) {
            return "用户名已存在";
        }

        SysUser newUser = new SysUser();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRealName(request.getRealName());
        // 默认普通用户角色 (USER)
        newUser.setRoleId(11L);
        newUser.setStatus((byte) 1);
        newUser.setCreateTime(LocalDateTime.now());

        int result = userMapper.insert(newUser);
        return result > 0 ? null : "注册失败,请稍后再试";
    }

    @Override
    public PageResult<UserListItemDto> listUsers(int page, int size, String keyword) {
        QueryWrapper<SysUser> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim();
            qw.and(w -> w.like("username", k).or().like("real_name", k));
        }
        qw.orderByDesc("create_time");

        Page<SysUser> p = userMapper.selectPage(new Page<>(page, size), qw);

        // 一次性查所有角色,内存 join(角色数量很少,11 个)
        Map<Long, SysRole> roleMap = new HashMap<>();
        for (SysRole r : roleMapper.selectList(null)) {
            roleMap.put(r.getId(), r);
        }

        List<UserListItemDto> records = new ArrayList<>();
        for (SysUser u : p.getRecords()) {
            UserListItemDto dto = toListItemDto(u, roleMap);
            records.add(dto);
        }
        return new PageResult<>(p.getTotal(), records);
    }

    @Override
    public String createUser(AdminUserCreateRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return "用户名不能为空";
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return "密码不能为空";
        }
        if (userMapper.findByUsername(request.getUsername()) != null) {
            return "用户名已存在";
        }

        SysUser u = new SysUser();
        u.setUsername(request.getUsername());
        u.setPassword(passwordEncoder.encode(request.getPassword()));
        u.setRealName(request.getRealName());
        u.setRoleId(request.getRoleId() == null ? 11L : request.getRoleId());
        u.setStatus((byte) 1);
        u.setCreateTime(LocalDateTime.now());
        int n = userMapper.insert(u);
        return n > 0 ? null : "创建失败";
    }

    @Override
    public String updateUser(Long userId, AdminUserUpdateRequest request) {
        SysUser u = userMapper.selectById(userId);
        if (u == null) return "用户不存在";

        if (request.getRealName() != null) u.setRealName(request.getRealName());
        if (request.getRoleId() != null) u.setRoleId(request.getRoleId());
        if (request.getStatus() != null) u.setStatus(request.getStatus());

        int n = userMapper.updateById(u);
        return n > 0 ? null : "更新失败";
    }

    @Override
    public String resetPassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return "新密码不能为空";
        }
        SysUser u = userMapper.selectById(userId);
        if (u == null) return "用户不存在";
        u.setPassword(passwordEncoder.encode(newPassword));
        int n = userMapper.updateById(u);
        return n > 0 ? null : "重置密码失败";
    }

    @Override
    public List<UserListItemDto> listByRoleCode(String roleCode) {
        if (roleCode == null || roleCode.trim().isEmpty()) {
            return List.of();
        }
        List<SysUser> users = userMapper.findByRoleCode(roleCode.trim());
        // 角色信息冗余只填角色 code/name 给前端展示(同一查询条件下都是同一个角色)
        Map<Long, SysRole> roleMap = new HashMap<>();
        for (SysRole r : roleMapper.selectList(null)) {
            roleMap.put(r.getId(), r);
        }
        List<UserListItemDto> result = new ArrayList<>();
        for (SysUser u : users) {
            UserListItemDto dto = toListItemDto(u, roleMap);
            result.add(dto);
        }
        return result;
    }

    private UserListItemDto toListItemDto(SysUser u, Map<Long, SysRole> roleMap) {
        UserListItemDto dto = new UserListItemDto();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setRealName(u.getRealName());
        dto.setPhone(u.getPhone());
        dto.setEmail(u.getEmail());
        dto.setRoleId(u.getRoleId());
        dto.setStatus(u.getStatus());
        dto.setCreateTime(u.getCreateTime() == null ? null : u.getCreateTime().format(DATE_FMT));
        SysRole r = roleMap.get(u.getRoleId());
        if (r != null) {
            dto.setRoleCode(r.getRoleCode());
            dto.setRoleName(r.getRoleName());
        }
        return dto;
    }
}
