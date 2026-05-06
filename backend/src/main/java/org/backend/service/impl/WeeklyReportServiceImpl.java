package org.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.backend.mapper.UserMapper;
import org.backend.mapper.WeeklyReportCommentMapper;
import org.backend.mapper.WeeklyReportMapper;
import org.backend.model.CrmWeeklyReport;
import org.backend.model.CrmWeeklyReportComment;
import org.backend.model.SysUser;
import org.backend.model.Dto.weekly.WeeklyReportDetailDto;
import org.backend.model.Dto.weekly.WeeklyReportListItemDto;
import org.backend.model.Dto.weekly.WeeklyReportSaveRequest;
import org.backend.service.WeeklyReportService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class WeeklyReportServiceImpl implements WeeklyReportService {

    @Autowired
    private WeeklyReportMapper reportMapper;
    @Autowired
    private WeeklyReportCommentMapper commentMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public List<WeeklyReportListItemDto> listMy(Long userId) {
        if (userId == null) return List.of();
        return reportMapper.selectListWithJoins(null, userId, null, null, null, null);
    }

    @Override
    public List<WeeklyReportListItemDto> listTeam(Long supervisorId) {
        if (supervisorId == null) return List.of();
        return reportMapper.selectListWithJoins(null, null, supervisorId, null, null, null);
    }

    @Override
    public WeeklyReportDetailDto detail(Long id) {
        if (id == null) return null;
        List<WeeklyReportListItemDto> rows = reportMapper.selectListWithJoins(id, null, null, null, null, null);
        if (rows.isEmpty()) return null;
        WeeklyReportListItemDto base = rows.get(0);
        WeeklyReportDetailDto dto = new WeeklyReportDetailDto();
        BeanUtils.copyProperties(base, dto);
        dto.setComments(commentMapper.selectByReportId(id));
        return dto;
    }

    @Override
    @Transactional
    public Long saveDraft(WeeklyReportSaveRequest req, Long currentUserId) {
        if (currentUserId == null) throw new IllegalArgumentException("未登录");
        if (req.getYear() == null || req.getWeekNum() == null) {
            throw new IllegalArgumentException("年份和周数必填");
        }

        // 找当前用户该周报(unique key uk_user_week)
        CrmWeeklyReport existing = reportMapper.selectOne(
                new LambdaQueryWrapper<CrmWeeklyReport>()
                        .eq(CrmWeeklyReport::getUserId, currentUserId)
                        .eq(CrmWeeklyReport::getYear, req.getYear())
                        .eq(CrmWeeklyReport::getWeekNum, req.getWeekNum())
        );

        if (existing != null) {
            if (!"DRAFT".equals(existing.getStatus())) {
                throw new IllegalArgumentException("该周已提交,不可再修改(业务规则 #19)");
            }
            existing.setAttendance(req.getAttendance());
            existing.setThisWeekWork(req.getThisWeekWork());
            existing.setNextWeekPlan(req.getNextWeekPlan());
            existing.setUpdateTime(LocalDateTime.now());
            reportMapper.updateById(existing);
            return existing.getId();
        }

        // 取主管 ID
        SysUser user = userMapper.selectById(currentUserId);
        Long supervisorId = user != null ? user.getSupervisorId() : null;
        if (supervisorId == null) {
            throw new IllegalArgumentException("当前用户未配置主管,无法提交周报");
        }

        CrmWeeklyReport r = new CrmWeeklyReport();
        r.setUserId(currentUserId);
        r.setSupervisorId(supervisorId);
        r.setYear(req.getYear());
        r.setWeekNum(req.getWeekNum());
        r.setAttendance(req.getAttendance());
        r.setThisWeekWork(req.getThisWeekWork());
        r.setNextWeekPlan(req.getNextWeekPlan());
        r.setStatus("DRAFT");
        r.setCreateTime(LocalDateTime.now());
        r.setUpdateTime(LocalDateTime.now());
        reportMapper.insert(r);
        return r.getId();
    }

    @Override
    public void submit(Long id, Long currentUserId) {
        CrmWeeklyReport r = reportMapper.selectById(id);
        if (r == null) throw new IllegalArgumentException("周报不存在: " + id);
        if (!Objects.equals(r.getUserId(), currentUserId)) {
            throw new IllegalArgumentException("无权操作他人周报");
        }
        if (!"DRAFT".equals(r.getStatus())) {
            throw new IllegalArgumentException("仅 DRAFT 状态可提交");
        }
        r.setStatus("SUBMITTED");
        r.setSubmitTime(LocalDateTime.now());
        r.setUpdateTime(LocalDateTime.now());
        reportMapper.updateById(r);
    }

    @Override
    @Transactional
    public void comment(Long id, String content, Long supervisorId) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("点评内容必填");
        }
        CrmWeeklyReport r = reportMapper.selectById(id);
        if (r == null) throw new IllegalArgumentException("周报不存在: " + id);
        if (!Objects.equals(r.getSupervisorId(), supervisorId)) {
            throw new IllegalArgumentException("仅该员工的主管可点评");
        }
        if ("DRAFT".equals(r.getStatus())) {
            throw new IllegalArgumentException("员工尚未提交,不可点评");
        }

        CrmWeeklyReportComment c = new CrmWeeklyReportComment();
        c.setReportId(id);
        c.setCommenterId(supervisorId);
        c.setContent(content);
        c.setCreateTime(LocalDateTime.now());
        commentMapper.insert(c);

        if (!"COMMENTED".equals(r.getStatus())) {
            r.setStatus("COMMENTED");
            r.setUpdateTime(LocalDateTime.now());
            reportMapper.updateById(r);
        }
    }

    @Override
    public org.backend.model.Dto.weekly.TeamSummaryDto teamSummary(Long supervisorId, Integer year, Integer weekNum) {
        if (supervisorId == null) throw new IllegalArgumentException("未登录");
        if (year == null || weekNum == null) throw new IllegalArgumentException("year/weekNum 必填");

        // 1. 查全部下属
        java.util.List<SysUser> subordinates = userMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getSupervisorId, supervisorId)
        );

        // 2. 查这周已落地的周报(可能是 DRAFT/SUBMITTED/COMMENTED)
        java.util.List<WeeklyReportListItemDto> reports =
                reportMapper.selectListWithJoins(null, null, supervisorId, null, year, weekNum);
        java.util.Map<Long, WeeklyReportListItemDto> byUser = new java.util.HashMap<>();
        for (WeeklyReportListItemDto r : reports) {
            byUser.put(r.getUserId(), r);
        }

        // 3. 组装 member 列表 + 计数
        org.backend.model.Dto.weekly.TeamSummaryDto dto = new org.backend.model.Dto.weekly.TeamSummaryDto();
        dto.setYear(year);
        dto.setWeekNum(weekNum);
        dto.setTotalCount(subordinates.size());

        int submittedCount = 0;
        int commentedCount = 0;
        java.util.List<org.backend.model.Dto.weekly.TeamSummaryDto.Member> members = new java.util.ArrayList<>();
        for (SysUser u : subordinates) {
            org.backend.model.Dto.weekly.TeamSummaryDto.Member m = new org.backend.model.Dto.weekly.TeamSummaryDto.Member();
            m.setUserId(u.getId());
            m.setUserName(u.getRealName() != null ? u.getRealName() : u.getUsername());
            WeeklyReportListItemDto r = byUser.get(u.getId());
            if (r != null) {
                m.setStatus(r.getStatus());
                m.setReportId(r.getId());
                m.setSubmitTime(r.getSubmitTime());
                if ("SUBMITTED".equals(r.getStatus()) || "COMMENTED".equals(r.getStatus())) {
                    submittedCount++;
                }
                if ("COMMENTED".equals(r.getStatus())) {
                    commentedCount++;
                }
            }
            members.add(m);
        }
        dto.setSubmittedCount(submittedCount);
        dto.setCommentedCount(commentedCount);
        dto.setMembers(members);
        return dto;
    }
}
