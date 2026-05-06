package org.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.backend.mapper.ContractMapper;
import org.backend.mapper.ContractPaymentMapper;
import org.backend.model.CrmContract;
import org.backend.model.CrmContractPayment;
import org.backend.model.Dto.contract.ContractCreateRequest;
import org.backend.model.Dto.contract.ContractDetailDto;
import org.backend.model.Dto.contract.ContractListItemDto;
import org.backend.model.Dto.contract.PaymentNodeRequest;
import org.backend.service.ContractService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ContractServiceImpl implements ContractService {

    @Autowired
    private ContractMapper contractMapper;
    @Autowired
    private ContractPaymentMapper paymentMapper;

    @Override
    public List<ContractListItemDto> list(String keyword, String customerName, String status, Integer year, String bu) {
        return contractMapper.selectListWithJoins(null, keyword, customerName, status, year, bu);
    }

    @Override
    public ContractDetailDto detail(Long id) {
        if (id == null) return null;
        List<ContractListItemDto> rows = contractMapper.selectListWithJoins(id, null, null, null, null, null);
        if (rows.isEmpty()) return null;
        ContractListItemDto base = rows.get(0);
        ContractDetailDto dto = new ContractDetailDto();
        BeanUtils.copyProperties(base, dto);
        dto.setPayments(paymentMapper.selectList(
                new LambdaQueryWrapper<CrmContractPayment>()
                        .eq(CrmContractPayment::getContractId, id)
                        .orderByAsc(CrmContractPayment::getPlanDate, CrmContractPayment::getId)
        ));
        return dto;
    }

    @Override
    @Transactional
    public Long create(ContractCreateRequest req, Long currentUserId) {
        // 1. 必填校验
        if (req.getContractName() == null || req.getContractName().isBlank()) {
            throw new IllegalArgumentException("合同名称必填");
        }
        if (req.getCustomerId() == null) throw new IllegalArgumentException("客户必填");
        if (req.getTotalAmount() == null) throw new IllegalArgumentException("合同金额必填");
        if (req.getContractYear() == null) throw new IllegalArgumentException("合同年份必填");
        if (req.getPaymentNodes() == null || req.getPaymentNodes().isEmpty()) {
            throw new IllegalArgumentException("至少需要一个付款节点");
        }

        // 2. 合同名唯一(DB UNIQUE 兜底,Service 提前校验给友好错误)
        Long dup = contractMapper.selectCount(
                new LambdaQueryWrapper<CrmContract>().eq(CrmContract::getContractName, req.getContractName())
        );
        if (dup != null && dup > 0) {
            throw new IllegalArgumentException("合同名称已存在,请换一个");
        }

        // 3. 总额一致性: total_amount == SUM(plan_amount)
        BigDecimal sum = BigDecimal.ZERO;
        for (PaymentNodeRequest n : req.getPaymentNodes()) {
            if (n.getNodeName() == null || n.getNodeName().isBlank()) {
                throw new IllegalArgumentException("付款节点名称必填");
            }
            if (n.getPlanAmount() == null || n.getPlanAmount().signum() <= 0) {
                throw new IllegalArgumentException("付款节点金额必须大于 0");
            }
            sum = sum.add(n.getPlanAmount());
        }
        if (sum.compareTo(req.getTotalAmount()) != 0) {
            throw new IllegalArgumentException("合同总额(" + req.getTotalAmount() + ")必须等于付款节点之和(" + sum + ")");
        }

        // 4. 落库
        CrmContract c = new CrmContract();
        c.setContractName(req.getContractName());
        c.setCustomerId(req.getCustomerId());
        c.setOppId(req.getOppId());
        c.setContractType(req.getContractType());
        c.setTotalAmount(req.getTotalAmount());
        c.setStatus("EXECUTING");
        c.setContractYear(req.getContractYear());
        c.setFileUrl(req.getFileUrl());
        c.setDeliveryTime(req.getDeliveryTime());
        c.setCreateBy(currentUserId);
        c.setCreateTime(LocalDateTime.now());
        c.setUpdateTime(LocalDateTime.now());
        contractMapper.insert(c);

        for (PaymentNodeRequest n : req.getPaymentNodes()) {
            CrmContractPayment p = new CrmContractPayment();
            p.setContractId(c.getId());
            p.setNodeName(n.getNodeName());
            p.setPlanAmount(n.getPlanAmount());
            p.setPlanDate(n.getPlanDate());
            p.setStatus(0);
            p.setCreateTime(LocalDateTime.now());
            paymentMapper.insert(p);
        }
        return c.getId();
    }

    @Override
    public void close(Long id, Long currentUserId) {
        CrmContract c = contractMapper.selectById(id);
        if (c == null) throw new IllegalArgumentException("合同不存在: " + id);
        if (Objects.equals(c.getStatus(), "COMPLETED")) {
            throw new IllegalArgumentException("已交付合同不可关闭(业务规则 #3)");
        }
        if (Objects.equals(c.getStatus(), "CLOSED")) {
            throw new IllegalArgumentException("合同已关闭");
        }
        c.setStatus("CLOSED");
        c.setCloseBy(currentUserId);
        c.setCloseTime(LocalDateTime.now());
        c.setUpdateTime(LocalDateTime.now());
        contractMapper.updateById(c);
    }

    @Override
    public void setDelivery(Long id, LocalDateTime deliveryTime, Long currentUserId) {
        if (deliveryTime == null) throw new IllegalArgumentException("验收时间必填");
        CrmContract c = contractMapper.selectById(id);
        if (c == null) throw new IllegalArgumentException("合同不存在: " + id);
        if (!Objects.equals(c.getStatus(), "EXECUTING")) {
            throw new IllegalArgumentException("仅执行中的合同可标记验收(当前: " + c.getStatus() + ")");
        }
        c.setDeliveryTime(deliveryTime);
        c.setUpdateTime(LocalDateTime.now());
        contractMapper.updateById(c);
        tryAutoComplete(id);
    }

    @Override
    public void tryAutoComplete(Long contractId) {
        if (contractId == null) return;
        CrmContract c = contractMapper.selectById(contractId);
        if (c == null) return;
        if (!Objects.equals(c.getStatus(), "EXECUTING")) return;
        if (c.getDeliveryTime() == null) return;

        // 任一未付节点 → 不推进
        Long unpaid = paymentMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CrmContractPayment>()
                        .eq(CrmContractPayment::getContractId, contractId)
                        .ne(CrmContractPayment::getStatus, 1)
        );
        if (unpaid != null && unpaid > 0) return;

        c.setStatus("COMPLETED");
        c.setUpdateTime(LocalDateTime.now());
        contractMapper.updateById(c);
    }

    @Override
    public List<ContractListItemDto> listInFlight(Integer year, String bu) {
        return contractMapper.selectListWithJoins(null, null, null, "EXECUTING", year, bu);
    }

    @Override
    public List<ContractListItemDto> listAccepted(Integer year, String bu) {
        return contractMapper.selectListWithJoins(null, null, null, "COMPLETED", year, bu);
    }
}
