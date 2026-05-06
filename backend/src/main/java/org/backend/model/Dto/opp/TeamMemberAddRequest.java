package org.backend.model.Dto.opp;

import lombok.Data;

@Data
public class TeamMemberAddRequest {
    private Long oppId;
    private Long userId;
    /** CORE 核心组 / SUPPORT 支撑组 */
    private String memberType;
    private String groupName;
    private String role;
}
