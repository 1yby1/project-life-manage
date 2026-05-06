package org.backend.model.Dto.opp;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商机组员 row(crm_opp_team) + join 出的姓名
 */
@Data
public class TeamMemberDto {
    private Long id;
    private Long oppId;
    private Long userId;
    private String userName;
    private String username;
    /** CORE 核心组 / SUPPORT 支撑组 */
    private String memberType;
    /** 分组名称(如 研发/测试/商务) */
    private String groupName;
    /** 组内职责 */
    private String role;
    private Long addBy;
    private String addByName;
    private LocalDateTime createTime;
}
