package org.backend.model.Dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class AdminUserCreateRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String realName;
    private Long roleId;
}
