package org.backend.model.Dto.lead;

import lombok.Data;

/**
 * 线索录入 - POST /leads
 * 录入后 entry_by = currentUser, status = ENTRY
 */
@Data
public class LeadCreateRequest {
    /** 客户 ID(必填) */
    private Long customerId;
    /** 线索名称(必填) */
    private String title;
    /** 线索描述(必填) */
    private String requirement;
    /** 业务单元(可选) */
    private String bu;
}
