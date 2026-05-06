package org.backend.service;

import org.backend.model.CrmCustomerVisit;
import org.backend.model.Dto.visit.VisitListItemDto;

import java.util.List;

/**
 * 客户走访 / 派单业务服务
 * <p>状态机: PENDING (派单) → DOING (首次写入 record) → COMPLETED (完成,不可逆)
 */
public interface CustomerVisitService {

    /**
     * 派单(OPP_ADMIN)
     * 校验: 客户存在 + 同一客户不可重复派单(不论 active visit 状态如何)
     */
    CrmCustomerVisit dispatch(Long customerId, Long managerId, Long assignByUserId);

    /**
     * 我的待走访清单(CUSTOMER_MANAGER)
     * @param status 可选过滤: PENDING/DOING/COMPLETED,null/空 不过滤
     */
    List<VisitListItemDto> myList(Long managerId, String status);

    /** 走访详情 */
    VisitListItemDto detail(Long visitId);

    /**
     * 暂存走访记录(CUSTOMER_MANAGER 仅自己; COMPLETED 拒绝)
     * 副作用: PENDING → DOING (首次)
     */
    void saveRecord(Long visitId, String visitRecord, Long currentUserId);

    /**
     * 完成走访(不可逆)
     * 副作用: status=COMPLETED, visitTime=now,后续 saveRecord/complete 全部 400
     */
    void complete(Long visitId, String visitRecord, Long currentUserId);
}
