package org.backend.model.Dto.lead;

import lombok.Data;

/**
 * 线索分发 - POST /leads/:id/distribute
 * 状态 COLLECTED → DISTRIBUTED, distributor_by = currentUser, manager_id = managerId
 */
@Data
public class LeadDistributeRequest {
    /** 被指派的客户经理 ID */
    private Long managerId;
}
