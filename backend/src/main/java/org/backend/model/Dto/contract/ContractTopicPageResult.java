package org.backend.model.Dto.contract;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 合同专题分页响应:在通用分页结构(records + total)之上,叠加全量汇总指标
 * 用于 PMO 已验收专题、区总在途专题这类"既要看明细,也要看全量金额合计"的场景。
 * <p>records / total 反映当前页;totalAmountSum / totalPaidSum 反映当前查询条件下的全部行。
 */
@Data
@NoArgsConstructor
public class ContractTopicPageResult {
    private long total;
    private List<ContractListItemDto> records;
    /** 全量合同金额合计(不受分页影响) */
    private BigDecimal totalAmountSum;
    /** 全量已收款合计(不受分页影响) */
    private BigDecimal totalPaidSum;

    public ContractTopicPageResult(long total, List<ContractListItemDto> records,
                                   BigDecimal totalAmountSum, BigDecimal totalPaidSum) {
        this.total = total;
        this.records = records;
        this.totalAmountSum = totalAmountSum == null ? BigDecimal.ZERO : totalAmountSum;
        this.totalPaidSum = totalPaidSum == null ? BigDecimal.ZERO : totalPaidSum;
    }
}
