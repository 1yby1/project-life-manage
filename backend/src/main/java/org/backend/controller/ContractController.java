package org.backend.controller;

import org.backend.model.CustomUserDetails;
import org.backend.model.Dto.contract.ContractCreateRequest;
import org.backend.model.Dto.contract.ContractDetailDto;
import org.backend.model.Dto.contract.ContractListItemDto;
import org.backend.service.ContractService;
import org.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contracts")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @GetMapping
    public Result<List<ContractListItemDto>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String bu) {
        return Result.success(contractService.list(keyword, customerName, status, year, bu));
    }

    @GetMapping("/{id}")
    public Result<ContractDetailDto> detail(@PathVariable Long id) {
        ContractDetailDto d = contractService.detail(id);
        if (d == null) return Result.error(404, "合同不存在");
        return Result.success(d);
    }

    @PostMapping
    @PreAuthorize("hasRole('OPP_ADMIN')")
    public Result<Long> create(@RequestBody ContractCreateRequest req,
                               @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            Long id = contractService.create(req, currentUser != null ? currentUser.getUserId() : null);
            return Result.success(id);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('OPP_ADMIN')")
    public Result<String> close(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            contractService.close(id, currentUser != null ? currentUser.getUserId() : null);
            return Result.success("合同已关闭");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /** 标记验收时间(OPP_ADMIN, EXECUTING) — 标记后若全付则自动 COMPLETED */
    @org.springframework.web.bind.annotation.PutMapping("/{id}/delivery")
    @PreAuthorize("hasRole('OPP_ADMIN')")
    public Result<String> setDelivery(@PathVariable Long id,
                                      @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> body,
                                      @AuthenticationPrincipal CustomUserDetails currentUser) {
        String raw = body == null ? null : body.get("deliveryTime");
        if (raw == null || raw.isBlank()) return Result.error(400, "deliveryTime 字段必填");
        java.time.LocalDateTime t;
        try {
            t = java.time.LocalDateTime.parse(raw);
        } catch (Exception e) {
            return Result.error(400, "deliveryTime 格式应为 ISO-8601 (yyyy-MM-ddTHH:mm:ss)");
        }
        try {
            contractService.setDelivery(id, t, currentUser != null ? currentUser.getUserId() : null);
            return Result.success("已标记验收");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /** 在途合同专题(REGION_HEAD: status=EXECUTING) */
    @GetMapping("/in-flight")
    @PreAuthorize("hasAnyRole('REGION_HEAD', 'OPP_ADMIN', 'ADMIN')")
    public Result<List<ContractListItemDto>> inFlight(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String bu) {
        return Result.success(contractService.listInFlight(year, bu));
    }

    /** 已验收项目专题(PMO: status=COMPLETED) */
    @GetMapping("/accepted")
    @PreAuthorize("hasAnyRole('PMO', 'OPP_ADMIN', 'ADMIN')")
    public Result<List<ContractListItemDto>> accepted(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String bu) {
        return Result.success(contractService.listAccepted(year, bu));
    }
}
