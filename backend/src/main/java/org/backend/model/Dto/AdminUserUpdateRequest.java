package org.backend.model.Dto;

import lombok.Data;

@Data
public class AdminUserUpdateRequest {
    private String realName;
    private Long roleId;
    private Byte status;
}
