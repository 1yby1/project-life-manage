package org.backend.service;

import org.backend.model.Dto.lead.LeadCollectRequest;
import org.backend.model.Dto.lead.LeadConvertResponse;
import org.backend.model.Dto.lead.LeadCreateRequest;
import org.backend.model.Dto.lead.LeadCultivateRequest;
import org.backend.model.Dto.lead.LeadDetailDto;
import org.backend.model.Dto.lead.LeadDistributeRequest;
import org.backend.model.Dto.lead.LeadListItemDto;

import java.util.List;

/**
 * 线索业务服务
 * <p>状态机: ENTRY → COLLECTED → DISTRIBUTED → CONVERTED
 * <p>本轮简化: 不引入 NURTURING 中间态(DISTRIBUTED 即"已派给客户经理可培育")
 */
public interface LeadService {

    /** 录入(任何已登录用户) */
    Long create(LeadCreateRequest req, Long currentUserId);

    /**
     * 清单
     * @param filter all / mine / participate / todo
     * @param currentUserId 用于 mine/participate/todo 计算
     * @param currentUserRoles 用于 todo(SALES/USER → ENTRY 待收集; OPP_ADMIN → COLLECTED 待分发; CUSTOMER_MANAGER → 自己的 DISTRIBUTED 待培育)
     */
    List<LeadListItemDto> list(String filter,
                               String keyword,
                               String bu,
                               String status,
                               Long currentUserId,
                               List<String> currentUserRoles);

    /** 详情(含培育 JSON 解析) */
    LeadDetailDto detail(Long leadId);

    /** 临时保存(entry_by==self 且 status=ENTRY) */
    void saveDraft(Long leadId, LeadCollectRequest req, Long currentUserId);

    /** 确认收集(entry_by==self 且 status=ENTRY → COLLECTED;collector_by 也填本人) */
    void collect(Long leadId, LeadCollectRequest req, Long currentUserId);

    /** 指派客户经理(OPP_ADMIN, status=COLLECTED → DISTRIBUTED) */
    void distribute(Long leadId, LeadDistributeRequest req, Long currentUserId);

    /** 完善培育详情(CUSTOMER_MANAGER 且 manager_id==self, status=DISTRIBUTED;可多次保存) */
    void cultivate(Long leadId, LeadCultivateRequest req, Long currentUserId);

    /** 转商机(CUSTOMER_MANAGER 且 manager_id==self, status=DISTRIBUTED → CONVERTED;创建 opportunity) */
    LeadConvertResponse convert(Long leadId, Long currentUserId);
}
