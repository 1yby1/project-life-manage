package org.backend.model.Dto.contract;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 标记付款节点已付
 */
@Data
public class PaymentSaveRequest {
    /** 实际到款金额(必填) */
    private BigDecimal actualAmount;
    /** 实际付款时间(可选,默认 now) */
    private LocalDateTime payTime;
}
