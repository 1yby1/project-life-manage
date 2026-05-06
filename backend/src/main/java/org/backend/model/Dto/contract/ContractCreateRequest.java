package org.backend.model.Dto.contract;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContractCreateRequest {
    /** 合同名称(必填,全局唯一) */
    private String contractName;
    /** 客户 ID(必填) */
    private Long customerId;
    /** 来源商机 ID(可选) */
    private Long oppId;
    /** 合同类型(下拉,可写死) */
    private String contractType;
    /** 合同总金额(必填,需 == 付款节点 plan_amount 之和) */
    private BigDecimal totalAmount;
    /** 合同年份(必填,首页按年筛选) */
    private Integer contractYear;
    /** 合同正文 URL(仅存,本轮不做上传) */
    private String fileUrl;
    /** 验收/交付时间(可选) */
    private LocalDateTime deliveryTime;
    /** 付款节点(必填,至少一个) */
    private List<PaymentNodeRequest> paymentNodes;
}
