package org.backend.service;

import org.backend.model.Dto.weekly.WeeklyReportDetailDto;
import org.backend.model.Dto.weekly.WeeklyReportListItemDto;
import org.backend.model.Dto.weekly.WeeklyReportSaveRequest;

import java.util.List;

public interface WeeklyReportService {

    /** 我的周报(SALES, user_id == self) */
    List<WeeklyReportListItemDto> listMy(Long userId);

    /** 下属周报(SUPERVISOR, supervisor_id == self) */
    List<WeeklyReportListItemDto> listTeam(Long supervisorId);

    /** 单条详情(含 comments[]) */
    WeeklyReportDetailDto detail(Long id);

    /**
     * 新建/更新草稿
     * <p>(user_id, year, week_num) 唯一,如果存在则按 status 决定:DRAFT 可改,SUBMITTED/COMMENTED 拒绝
     */
    Long saveDraft(WeeklyReportSaveRequest req, Long currentUserId);

    /** 提交(SALES, DRAFT → SUBMITTED, 之后不可改) */
    void submit(Long id, Long currentUserId);

    /** 点评(SUPERVISOR, SUBMITTED → COMMENTED + 创 comment 记录) */
    void comment(Long id, String content, Long supervisorId);

    /**
     * 主管 dashboard: 某周下属提交/点评聚合
     * <p>下属 = sys_user.supervisor_id == supervisorId 的全部账号
     */
    org.backend.model.Dto.weekly.TeamSummaryDto teamSummary(Long supervisorId, Integer year, Integer weekNum);
}
