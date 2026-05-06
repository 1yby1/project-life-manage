package org.backend.model.Dto;

import lombok.Data;

@Data
public class UserListItemDto {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private Long roleId;
    private String roleCode;
    private String roleName;
    private Byte status;
    private String createTime;
}
