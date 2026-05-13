package org.backend.model.Dto.contract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 合同聚合行(用于按年度 / 按 BU 等维度分组的统计图表)
 * dimension 字段对应分组键(year 是数字字符串、bu 是字符串),由调用方决定语义。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractDimensionAggregate {
    private String dimension;
    private long count;
    private BigDecimal totalAmount;
    private BigDecimal totalPaid;
}
