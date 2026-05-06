package org.backend.service.impl;

import org.backend.mapper.ContractPaymentMapper;
import org.backend.model.CrmContractPayment;
import org.backend.model.Dto.contract.PaymentSaveRequest;
import org.backend.service.ContractPaymentService;
import org.backend.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ContractPaymentServiceImpl implements ContractPaymentService {

    @Autowired
    private ContractPaymentMapper paymentMapper;
    @Autowired
    private ContractService contractService;

    @Override
    public void markPaid(Long paymentId, PaymentSaveRequest req, Long currentUserId) {
        CrmContractPayment p = paymentMapper.selectById(paymentId);
        if (p == null) throw new IllegalArgumentException("付款节点不存在: " + paymentId);
        if (p.getStatus() != null && p.getStatus() == 1) {
            throw new IllegalArgumentException("该节点已标记已付");
        }
        if (req.getActualAmount() == null || req.getActualAmount().signum() <= 0) {
            throw new IllegalArgumentException("实际到款金额必须大于 0");
        }

        p.setActualAmount(req.getActualAmount());
        p.setStatus(1);
        p.setPayTime(req.getPayTime() != null ? req.getPayTime() : LocalDateTime.now());
        paymentMapper.updateById(p);

        // 自然完成钩子: 全付 + delivery_time 已设 → 合同推进 EXECUTING → COMPLETED
        contractService.tryAutoComplete(p.getContractId());
    }
}
