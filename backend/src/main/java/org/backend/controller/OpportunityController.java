package org.backend.controller;

import org.backend.model.CustomUserDetails;
import org.backend.model.Dto.opp.ApplyTemplateRequest;
import org.backend.model.Dto.opp.OppDetailDto;
import org.backend.model.Dto.opp.OppListItemDto;
import org.backend.model.Dto.opp.StageOwnerRequest;
import org.backend.service.OpportunityService;
import org.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/opportunities")
public class OpportunityController {

    @Autowired
    private OpportunityService oppService;

    @GetMapping
    public Result<List<OppListItemDto>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String stage) {
        return Result.success(oppService.list(keyword, customerId, stage));
    }

    @GetMapping("/{id}")
    public Result<OppDetailDto> detail(@PathVariable Long id) {
        OppDetailDto d = oppService.detail(id);
        if (d == null) return Result.error(404, "商机不存在");
        return Result.success(d);
    }

    /** 选模板,生成 stage 实例(仅 PM,即 opp.pm_id == currentUser) */
    @PostMapping("/{id}/apply-template")
    public Result<String> applyTemplate(@PathVariable Long id,
                                        @RequestBody ApplyTemplateRequest req,
                                        @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            oppService.applyTemplate(id, req.getTemplateId(),
                    currentUser != null ? currentUser.getUserId() : null);
            return Result.success("模板已应用,环节已生成");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /** 设置环节责任人(PM 全部 / SM/CM/DM 仅自己环节) */
    @PutMapping("/stages/{stageId}/owner")
    public Result<String> setStageOwner(@PathVariable Long stageId,
                                        @RequestBody StageOwnerRequest req,
                                        @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            oppService.setStageOwner(stageId, req.getOwnerId(),
                    currentUser != null ? currentUser.getUserId() : null);
            return Result.success("已更新环节责任人");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /** 推进商机阶段(VALIDATE → NEGOTIATE → IMPLEMENT → DELIVERY,仅 PM,顺序推进) */
    @PostMapping("/{id}/advance-stage")
    public Result<String> advanceStage(@PathVariable Long id,
                                       @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            oppService.advanceStage(id, currentUser != null ? currentUser.getUserId() : null);
            return Result.success("已推进到下一阶段");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
