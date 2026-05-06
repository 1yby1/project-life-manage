package org.backend.model.Dto.contract;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.backend.model.CrmContractPayment;

import java.util.List;

/**
 * 合同详情 = 列表 row + 付款节点列表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ContractDetailDto extends ContractListItemDto {
    private List<CrmContractPayment> payments;
}
