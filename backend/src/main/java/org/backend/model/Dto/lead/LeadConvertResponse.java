package org.backend.model.Dto.lead;

import lombok.Data;

/**
 * 转商机响应 - POST /leads/:id/convert
 */
@Data
public class LeadConvertResponse {
    /** 新建商机的 ID */
    private Long opportunityId;
    /** 新建商机名称 */
    private String opportunityName;
}
