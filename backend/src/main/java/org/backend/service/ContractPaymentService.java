package org.backend.service;

import org.backend.model.Dto.contract.PaymentSaveRequest;

public interface ContractPaymentService {
    /** 标记付款节点已付(OPP_ADMIN) */
    void markPaid(Long paymentId, PaymentSaveRequest req, Long currentUserId);
}
