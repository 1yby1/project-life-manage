package org.backend.model.Dto.contract;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.backend.model.CrmContract;

import java.math.BigDecimal;

/**
 * 合同列表 row(crm_contract) + join 出的客户名/创建人/关闭人/商机名 + 已付聚合
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ContractListItemDto extends CrmContract {
    private String customerName;
    private String creatorName;
    private String closerName;
    private String oppName;
    /** 已付总额(SUM crm_contract_payment.actual_amount WHERE status=1) */
    private BigDecimal paidAmount;
}
