package org.backend.model.Dto.lead;

import lombok.Data;

/**
 * 线索收集 - PUT /leads/:id (临时保存) 或 POST /leads/:id/collect (确认收集)
 * 收集人 = 录入人本身; 状态 ENTRY → COLLECTED (仅 collect 端点切换)
 */
@Data
public class LeadCollectRequest {
    private Long customerId;
    private String title;
    private String requirement;
}
