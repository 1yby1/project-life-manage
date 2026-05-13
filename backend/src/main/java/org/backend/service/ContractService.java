package org.backend.service;

import org.backend.model.Dto.PageResult;
import org.backend.model.Dto.contract.ContractCreateRequest;
import org.backend.model.Dto.contract.ContractDetailDto;
import org.backend.model.Dto.contract.ContractDimensionAggregate;
import org.backend.model.Dto.contract.ContractListItemDto;
import org.backend.model.Dto.contract.ContractTopicPageResult;

import java.time.LocalDateTime;
import java.util.List;

public interface ContractService {

    /** 分页列表(query: keyword/customerName/status/year/bu) */
    PageResult<ContractListItemDto> list(long page, long size, String keyword, String customerName, String status, Integer year, String bu);

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

    /** 在途专题分页(REGION_HEAD, status=EXECUTING);响应附带全量金额汇总,供 KPI 使用 */
    ContractTopicPageResult listInFlight(long page, long size, Integer year, String bu);

    /** 已验收专题分页(PMO, status=COMPLETED);响应附带全量金额汇总,供 KPI 使用 */
    ContractTopicPageResult listAccepted(long page, long size, Integer year, String bu);

    /** 已验收专题按年度聚合(用于柱状图) */
    List<ContractDimensionAggregate> acceptedAggregateByYear(String bu);

    /** 在途专题按 BU 聚合(用于堆叠柱状图) */
    List<ContractDimensionAggregate> inFlightAggregateByBu(Integer year);
}
