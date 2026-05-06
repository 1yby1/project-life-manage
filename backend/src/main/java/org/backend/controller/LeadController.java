package org.backend.controller;

import org.backend.model.CustomUserDetails;
import org.backend.model.Dto.lead.LeadCollectRequest;
import org.backend.model.Dto.lead.LeadConvertResponse;
import org.backend.model.Dto.lead.LeadCreateRequest;
import org.backend.model.Dto.lead.LeadCultivateRequest;
import org.backend.model.Dto.lead.LeadDetailDto;
import org.backend.model.Dto.lead.LeadDistributeRequest;
import org.backend.model.Dto.lead.LeadListItemDto;
import org.backend.service.LeadService;
import org.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 线索 REST 接口
 * <p>对应需求文档第二节模块 3 + 第六节 6.1 状态机
 * <p>权限: 录入/查看 任何已登录;收集 entry_by 本人;分发 OPP_ADMIN;培育/转商机 manager_id 本人(必须 CUSTOMER_MANAGER)
 */
@RestController
@RequestMapping("/leads")
public class LeadController {

    @Autowired
    private LeadService leadService;

    /** 录入线索(任何已登录用户) */
    @PostMapping
    public Result<Long> create(@RequestBody LeadCreateRequest req,
                               @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            Long id = leadService.create(req, currentUser != null ? currentUser.getUserId() : null);
            return Result.success(id);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 线索清单
     * @param filter all/mine/participate/todo
     */
    @GetMapping
    public Result<List<LeadListItemDto>> list(
            @RequestParam(required = false, defaultValue = "all") String filter,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String bu,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser != null ? currentUser.getUserId() : null;
        List<String> roles = currentUser != null ? currentUser.getRoleCodes() : Collections.emptyList();
        return Result.success(leadService.list(filter, keyword, bu, status, userId, roles));
    }

    /** 详情(含培育详情解析) */
    @GetMapping("/{id}")
    public Result<LeadDetailDto> detail(@PathVariable Long id) {
        LeadDetailDto detail = leadService.detail(id);
        if (detail == null) return Result.error(404, "线索不存在");
        return Result.success(detail);
    }

    /** 临时保存(entry_by 本人 + ENTRY) */
    @PutMapping("/{id}")
    public Result<String> saveDraft(@PathVariable Long id,
                                    @RequestBody LeadCollectRequest req,
                                    @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            leadService.saveDraft(id, req, currentUser != null ? currentUser.getUserId() : null);
            return Result.success("保存成功");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /** 确认收集(entry_by 本人 + ENTRY → COLLECTED) */
    @PostMapping("/{id}/collect")
    public Result<String> collect(@PathVariable Long id,
                                  @RequestBody LeadCollectRequest req,
                                  @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            leadService.collect(id, req, currentUser != null ? currentUser.getUserId() : null);
            return Result.success("收集成功,已进入分发阶段");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /** 分发(OPP_ADMIN, COLLECTED → DISTRIBUTED) */
    @PostMapping("/{id}/distribute")
    @PreAuthorize("hasRole('OPP_ADMIN')")
    public Result<String> distribute(@PathVariable Long id,
                                     @RequestBody LeadDistributeRequest req,
                                     @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            leadService.distribute(id, req, currentUser != null ? currentUser.getUserId() : null);
            return Result.success("指派成功,线索已进入培育阶段");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /** 培育(CUSTOMER_MANAGER, manager_id 本人, DISTRIBUTED, 可多次保存) */
    @PutMapping("/{id}/cultivate")
    @PreAuthorize("hasRole('CUSTOMER_MANAGER')")
    public Result<String> cultivate(@PathVariable Long id,
                                    @RequestBody LeadCultivateRequest req,
                                    @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            leadService.cultivate(id, req, currentUser != null ? currentUser.getUserId() : null);
            return Result.success("保存成功");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /** 转商机(CUSTOMER_MANAGER, manager_id 本人, DISTRIBUTED → CONVERTED + 创建商机占位) */
    @PostMapping("/{id}/convert")
    @PreAuthorize("hasRole('CUSTOMER_MANAGER')")
    public Result<LeadConvertResponse> convert(@PathVariable Long id,
                                               @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            LeadConvertResponse resp = leadService.convert(id, currentUser != null ? currentUser.getUserId() : null);
            return Result.success(resp);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
