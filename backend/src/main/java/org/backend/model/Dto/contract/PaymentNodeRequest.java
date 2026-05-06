package org.backend.model.Dto.contract;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建合同时嵌入的付款节点
 */
@Data
public class PaymentNodeRequest {
    private String nodeName;
    private BigDecimal planAmount;
    private LocalDate planDate;
}
