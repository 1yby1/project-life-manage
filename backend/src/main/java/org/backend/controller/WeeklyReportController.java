package org.backend.controller;

import org.backend.model.CustomUserDetails;
import org.backend.model.Dto.weekly.WeeklyReportCommentRequest;
import org.backend.model.Dto.weekly.WeeklyReportDetailDto;
import org.backend.model.Dto.weekly.WeeklyReportListItemDto;
import org.backend.model.Dto.weekly.WeeklyReportSaveRequest;
import org.backend.service.WeeklyReportService;
import org.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/weekly-reports")
public class WeeklyReportController {

    @Autowired
    private WeeklyReportService reportService;

    /** 我的周报(SALES) */
    @GetMapping("/my")
    @PreAuthorize("hasRole('SALES')")
    public Result<List<WeeklyReportListItemDto>> myReports(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser != null ? currentUser.getUserId() : null;
        return Result.success(reportService.listMy(userId));
    }

    /** 下属周报(SUPERVISOR) */
    @GetMapping("/team")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public Result<List<WeeklyReportListItemDto>> teamReports(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long supervisorId = currentUser != null ? currentUser.getUserId() : null;
        return Result.success(reportService.listTeam(supervisorId));
    }

    /** 详情(含 comments[]) */
    @GetMapping("/{id}")
    public Result<WeeklyReportDetailDto> detail(@PathVariable Long id) {
        WeeklyReportDetailDto d = reportService.detail(id);
        if (d == null) return Result.error(404, "周报不存在");
        return Result.success(d);
    }

    /** 新建/更新草稿 */
    @PostMapping("/draft")
    @PreAuthorize("hasRole('SALES')")
    public Result<Long> saveDraft(@RequestBody WeeklyReportSaveRequest req,
                                  @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            Long id = reportService.saveDraft(req, currentUser != null ? currentUser.getUserId() : null);
            return Result.success(id);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /** 提交(DRAFT → SUBMITTED) */
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('SALES')")
    public Result<String> submit(@PathVariable Long id,
                                 @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            reportService.submit(id, currentUser != null ? currentUser.getUserId() : null);
            return Result.success("已提交,等待主管点评");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /** 主管点评(SUBMITTED → COMMENTED) */
    @PostMapping("/{id}/comment")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public Result<String> comment(@PathVariable Long id,
                                  @RequestBody WeeklyReportCommentRequest req,
                                  @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            reportService.comment(id, req.getContent(), currentUser != null ? currentUser.getUserId() : null);
            return Result.success("点评已提交");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /** 主管 dashboard: 某周下属提交/点评聚合 */
    @GetMapping("/team/summary")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public Result<org.backend.model.Dto.weekly.TeamSummaryDto> teamSummary(
            @org.springframework.web.bind.annotation.RequestParam Integer year,
            @org.springframework.web.bind.annotation.RequestParam Integer weekNum,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            Long supervisorId = currentUser != null ? currentUser.getUserId() : null;
            return Result.success(reportService.teamSummary(supervisorId, year, weekNum));
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
