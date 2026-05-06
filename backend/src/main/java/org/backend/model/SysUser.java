package org.backend.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 直属主管 ID, 用于周报点评 */
    private Long supervisorId;

    /** 主角色 ID(兼容旧代码,实际权限以 sys_user_role 为准) */
    private Long roleId;

    /** 1 正常 / 0 禁用 */
    private byte status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

