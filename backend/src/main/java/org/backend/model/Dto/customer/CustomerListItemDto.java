package org.backend.model.Dto.customer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.backend.model.CrmCustomer;

/**
 * 客户列表 / 详情 row,在 {@link CrmCustomer} 基础上扩展派单状态派生字段。
 * - visitStatus null 表示尚未派单(对应前端"已录入"阶段)
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CustomerListItemDto extends CrmCustomer {
    /** 派生字段:当前走访状态 PENDING/DOING/COMPLETED,null 表示未派单 */
    private String visitStatus;
    /** 派生字段:当前指派的客户经理 ID */
    private Long assignedManagerId;
    /** 派生字段:当前指派的客户经理姓名 */
    private String assignedManagerName;
}
