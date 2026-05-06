package org.backend.service;

import org.backend.model.Dto.contract.ContractCreateRequest;
import org.backend.model.Dto.contract.ContractDetailDto;
import org.backend.model.Dto.contract.ContractListItemDto;

import java.time.LocalDateTime;
import java.util.List;

public interface ContractService {

    /** 列表(query: keyword/customerName/status/year/bu) */
    List<ContractListItemDto> list(String keyword, String customerName, String status, Integer year, String bu);

    /** 详情(含付款节点) */
    ContractDetailDto detail(Long id);

    /** 新建合同(OPP_ADMIN, 校验合同名唯一 + 总额 == 节点总和) */
    Long create(ContractCreateRequest req, Long currentUserId);

    /** 关闭合同(OPP_ADMIN, 仅 EXECUTING; status → CLOSED) */
    void close(Long id, Long currentUserId);

    /**
     * 标记验收时间(OPP_ADMIN, 仅 EXECUTING)
     * <p>标记后立即触发 {@link #tryAutoComplete}: 若全部 payments 已付,合同自动 COMPLETED
     */
    void setDelivery(Long id, LocalDateTime deliveryTime, Long currentUserId);

    /**
     * 自动完成钩子: status=EXECUTING + 全部 payments status=1 + delivery_time!=null → 推进到 COMPLETED
     * <p>幂等,任何条件不满足都直接 return,无副作用
     * <p>由 setDelivery + ContractPaymentService.markPaid 调用
     */
    void tryAutoComplete(Long contractId);

    /** 在途专题(REGION_HEAD, status=EXECUTING) */
    List<ContractListItemDto> listInFlight(Integer year, String bu);

    /** 已验收专题(PMO, status=COMPLETED) */
    List<ContractListItemDto> listAccepted(Integer year, String bu);
}
