package org.backend.model.Dto.lead;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 线索详情 - GET /leads/:id
 * <p>在 LeadListItemDto 基础上,把 progress_desc 解析为结构化 cultivateInfo 对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LeadDetailDto extends LeadListItemDto {
    /** 培育详情(progress_desc JSON 反序列化结果);未培育则 null */
    private LeadCultivateRequest cultivateInfo;
}
