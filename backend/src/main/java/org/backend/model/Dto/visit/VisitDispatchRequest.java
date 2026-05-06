package org.backend.model.Dto.visit;

import lombok.Data;

/**
 * 派单请求 - POST /customer-visits
 */
@Data
public class VisitDispatchRequest {
    /** 客户 ID */
    private Long customerId;
    /** 被派单的客户经理 ID */
    private Long managerId;
}
