package org.backend.controller;

import org.backend.model.CustomUserDetails;
import org.backend.model.Dto.contract.PaymentSaveRequest;
import org.backend.service.ContractPaymentService;
import org.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contract-payments")
public class ContractPaymentController {

    @Autowired
    private ContractPaymentService paymentService;

    /** 标记付款节点已付(OPP_ADMIN) */
    @PutMapping("/{id}/pay")
    @PreAuthorize("hasRole('OPP_ADMIN')")
    public Result<String> markPaid(@PathVariable Long id,
                                   @RequestBody PaymentSaveRequest req,
                                   @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            paymentService.markPaid(id, req, currentUser != null ? currentUser.getUserId() : null);
            return Result.success("已标记已付");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
