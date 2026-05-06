package org.backend.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.backend.mapper.CustomerMapper;
import org.backend.mapper.CustomerVisitMapper;
import org.backend.mapper.UserMapper;
import org.backend.model.CrmCustomer;
import org.backend.model.CrmCustomerVisit;
import org.backend.model.Dto.PageResult;
import org.backend.model.Dto.customer.CustomerListItemDto;
import org.backend.model.Dto.customer.CustomerSaveRequest;
import org.backend.model.SysUser;
import org.backend.service.CustomerService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private CustomerVisitMapper customerVisitMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageResult<CustomerListItemDto> list(long page, long size, String keyword, String city, String visitStatus) {
        Page<CustomerListItemDto> p = new Page<>(page, size);
        IPage<CustomerListItemDto> result = customerMapper.selectListWithVisitStatus(p, keyword, city, visitStatus);
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    @Override
    public CustomerListItemDto detail(Long id) {
        if (id == null) return null;
        CrmCustomer customer = customerMapper.selectById(id);
        if (customer == null) return null;

        CustomerListItemDto dto = new CustomerListItemDto();
        BeanUtils.copyProperties(customer, dto);

        // 派生字段: 当前的 active visit + 客户经理姓名
        CrmCustomerVisit visit = customerVisitMapper.findActiveByCustomerId(id);
        if (visit != null) {
            dto.setVisitStatus(visit.getStatus());
            dto.setAssignedManagerId(visit.getManagerId());
            if (visit.getManagerId() != null) {
                SysUser manager = userMapper.selectById(visit.getManagerId());
                if (manager != null) {
                    dto.setAssignedManagerName(manager.getRealName());
                }
            }
        }
        return dto;
    }

    @Override
    public CrmCustomer create(CustomerSaveRequest req, Long currentUserId) {
        validateRequired(req);

        CrmCustomer entity = new CrmCustomer();
        BeanUtils.copyProperties(req, entity);
        entity.setCreateBy(currentUserId);
        entity.setStatus(1);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        customerMapper.insert(entity);
        return entity;
    }

    @Override
    public CrmCustomer update(Long id, CustomerSaveRequest req) {
        validateRequired(req);

        CrmCustomer existing = customerMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("客户不存在: " + id);
        }

        BeanUtils.copyProperties(req, existing);
        existing.setUpdateTime(LocalDateTime.now());
        customerMapper.updateById(existing);
        return existing;
    }

    private void validateRequired(CustomerSaveRequest req) {
        if (req.getCustomerName() == null || req.getCustomerName().isBlank()) {
            throw new IllegalArgumentException("客户名称必填");
        }
        if (req.getCity() == null || req.getCity().isBlank()) {
            throw new IllegalArgumentException("地市必填");
        }
        if (req.getLegalPerson() == null || req.getLegalPerson().isBlank()) {
            throw new IllegalArgumentException("法人必填");
        }
    }
}
