package org.backend.controller;

import org.backend.model.CrmCustomerVisit;
import org.backend.model.CustomUserDetails;
import org.backend.model.Dto.visit.VisitDispatchRequest;
import org.backend.model.Dto.visit.VisitListItemDto;
import org.backend.model.Dto.visit.VisitRecordRequest;
import org.backend.service.CustomerVisitService;
import org.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户走访 / 派单 REST 接口
 * <p>对应需求文档第二节"模块 1 客户管理"中的派单 / 走访录入 / 完成走访 子流程,
 * 以及第八节关键业务规则#2 走访不可逆 + 新规则#12 派单唯一性。
 */
@RestController
@RequestMapping("/customer-visits")
public class CustomerVisitController {

    @Autowired
    private CustomerVisitService visitService;

    /**
     * 派单(OPP_ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('OPP_ADMIN')")
    public Result<CrmCustomerVisit> dispatch(
            @RequestBody VisitDispatchRequest req,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            Long assignByUserId = currentUser != null ? currentUser.getUserId() : null;
            CrmCustomerVisit visit = visitService.dispatch(req.getCustomerId(), req.getManagerId(), assignByUserId);
            return Result.success(visit);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 我的待走访清单(CUSTOMER_MANAGER,自动过滤当前用户)
     * @param status 可选 PENDING/DOING/COMPLETED
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER_MANAGER')")
    public Result<List<VisitListItemDto>> myList(
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser != null ? currentUser.getUserId() : null;
        return Result.success(visitService.myList(userId, status));
    }

    /**
     * 走访详情
     */
    @GetMapping("/{id}")
    public Result<VisitListItemDto> detail(@PathVariable Long id) {
        VisitListItemDto v = visitService.detail(id);
        if (v == null) {
            return Result.error(404, "走访记录不存在");
        }
        return Result.success(v);
    }

    /**
     * 暂存走访记录(CUSTOMER_MANAGER 自己的走访;COMPLETED 拒绝)
     */
    @PutMapping("/{id}/record")
    @PreAuthorize("hasRole('CUSTOMER_MANAGER')")
    public Result<String> saveRecord(
            @PathVariable Long id,
            @RequestBody VisitRecordRequest req,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            visitService.saveRecord(id, req.getVisitRecord(), currentUser != null ? currentUser.getUserId() : null);
            return Result.success("保存成功");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 完成走访(不可逆)
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('CUSTOMER_MANAGER')")
    public Result<String> complete(
            @PathVariable Long id,
            @RequestBody VisitRecordRequest req,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            visitService.complete(id, req.getVisitRecord(), currentUser != null ? currentUser.getUserId() : null);
            return Result.success("完成走访成功");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
