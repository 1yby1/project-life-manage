package org.backend.service.impl;

import org.backend.mapper.CustomerMapper;
import org.backend.mapper.CustomerVisitMapper;
import org.backend.model.CrmCustomer;
import org.backend.model.CrmCustomerVisit;
import org.backend.model.Dto.visit.VisitListItemDto;
import org.backend.service.CustomerVisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerVisitServiceImpl implements CustomerVisitService {

    @Autowired
    private CustomerVisitMapper visitMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Override
    public CrmCustomerVisit dispatch(Long customerId, Long managerId, Long assignByUserId) {
        if (customerId == null) throw new IllegalArgumentException("客户 ID 必填");
        if (managerId == null) throw new IllegalArgumentException("客户经理 ID 必填");

        CrmCustomer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("客户不存在: " + customerId);
        }

        // 派单唯一性: 同一客户不可重复派单(任何 active visit 都阻断)
        CrmCustomerVisit existing = visitMapper.findActiveByCustomerId(customerId);
        if (existing != null) {
            String hint = "PENDING".equals(existing.getStatus()) ? "已派单待走访"
                    : "DOING".equals(existing.getStatus()) ? "正在走访中"
                    : "走访已完成";
            throw new IllegalArgumentException("该客户" + hint + ",不可重复派单");
        }

        CrmCustomerVisit visit = new CrmCustomerVisit();
        visit.setCustomerId(customerId);
        visit.setManagerId(managerId);
        visit.setAssignBy(assignByUserId);
        visit.setStatus("PENDING");
        visit.setAssignTime(LocalDateTime.now());
        visit.setCreateTime(LocalDateTime.now());
        visit.setUpdateTime(LocalDateTime.now());

        visitMapper.insert(visit);
        return visit;
    }

    @Override
    public List<VisitListItemDto> myList(Long managerId, String status) {
        if (managerId == null) {
            return List.of();
        }
        return visitMapper.selectListWithJoins(managerId, status);
    }

    @Override
    public VisitListItemDto detail(Long visitId) {
        if (visitId == null) return null;
        return visitMapper.findByIdWithJoins(visitId);
    }

    @Override
    public void saveRecord(Long visitId, String visitRecord, Long currentUserId) {
        CrmCustomerVisit visit = mustLoad(visitId);
        ensureOwner(visit, currentUserId);
        ensureMutable(visit);

        visit.setVisitRecord(visitRecord);
        if ("PENDING".equals(visit.getStatus())) {
            visit.setStatus("DOING");
        }
        visit.setUpdateTime(LocalDateTime.now());
        visitMapper.updateById(visit);
    }

    @Override
    public void complete(Long visitId, String visitRecord, Long currentUserId) {
        CrmCustomerVisit visit = mustLoad(visitId);
        ensureOwner(visit, currentUserId);
        ensureMutable(visit);

        if (visitRecord != null) {
            visit.setVisitRecord(visitRecord);
        }
        visit.setStatus("COMPLETED");
        visit.setVisitTime(LocalDateTime.now());
        visit.setUpdateTime(LocalDateTime.now());
        visitMapper.updateById(visit);
    }

    private CrmCustomerVisit mustLoad(Long visitId) {
        if (visitId == null) {
            throw new IllegalArgumentException("走访 ID 必填");
        }
        CrmCustomerVisit visit = visitMapper.selectById(visitId);
        if (visit == null) {
            throw new IllegalArgumentException("走访记录不存在: " + visitId);
        }
        return visit;
    }

    private void ensureOwner(CrmCustomerVisit visit, Long currentUserId) {
        if (currentUserId == null || !currentUserId.equals(visit.getManagerId())) {
            throw new IllegalArgumentException("无权操作他人的走访记录");
        }
    }

    private void ensureMutable(CrmCustomerVisit visit) {
        if ("COMPLETED".equals(visit.getStatus())) {
            throw new IllegalArgumentException("该走访已完成,不可再修改(走访不可逆)");
        }
    }
}
