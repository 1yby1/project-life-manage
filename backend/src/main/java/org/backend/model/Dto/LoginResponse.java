package org.backend.model.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String token;
    private Long userId;
    private String username;
    /** 真实姓名(展示用) */
    private String realName;
    /** 主角色 ID(兼容旧前端,实际权限以 roles 为准) */
    private Long roleId;
    /** 全部角色编码,如 ["ADMIN","USER"] */
    private List<String> roles;
}
