package org.backend.service;

import org.backend.model.CrmCustomer;
import org.backend.model.Dto.PageResult;
import org.backend.model.Dto.customer.CustomerListItemDto;
import org.backend.model.Dto.customer.CustomerSaveRequest;

public interface CustomerService {

    /**
     * 分页查询客户列表(含派单状态派生字段)
     * @param visitStatus 走访状态过滤: PENDING/DOING/COMPLETED/NONE(未派单),null/空 不过滤
     */
    PageResult<CustomerListItemDto> list(long page, long size, String keyword, String city, String visitStatus);

    /** 查询单个客户(含派单状态派生字段) */
    CustomerListItemDto detail(Long id);

    /** 创建客户 */
    CrmCustomer create(CustomerSaveRequest req, Long currentUserId);

    /** 更新客户 */
    CrmCustomer update(Long id, CustomerSaveRequest req);
}
