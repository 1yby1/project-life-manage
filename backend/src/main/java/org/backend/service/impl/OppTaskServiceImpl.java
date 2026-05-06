package org.backend.service.impl;

import org.backend.mapper.OppTaskMapper;
import org.backend.mapper.OpportunityMapper;
import org.backend.model.CrmOppTask;
import org.backend.model.CrmOpportunity;
import org.backend.model.Dto.opp.TaskCreateRequest;
import org.backend.model.Dto.opp.TaskListItemDto;
import org.backend.model.Dto.opp.TaskUpdateRequest;
import org.backend.service.OppTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class OppTaskServiceImpl implements OppTaskService {

    @Autowired
    private OppTaskMapper taskMapper;
    @Autowired
    private OpportunityMapper opportunityMapper;

    @Override
    public List<TaskListItemDto> list(Long oppId, Long stageId) {
        return taskMapper.selectListWithJoins(oppId, stageId);
    }

    @Override
    public Long create(TaskCreateRequest req, Long currentUserId) {
        if (req.getOppId() == null) throw new IllegalArgumentException("商机 ID 必填");
        if (req.getTaskName() == null || req.getTaskName().isBlank()) {
            throw new IllegalArgumentException("任务名称必填");
        }
        if (!isIronTriangle(req.getOppId(), currentUserId)) {
            throw new IllegalArgumentException("仅商机的项目经理/解决方案经理/交付经理/负责人可创建任务");
        }

        CrmOppTask t = new CrmOppTask();
        t.setOppId(req.getOppId());
        t.setStageId(req.getStageId());
        t.setTaskName(req.getTaskName());
        t.setContent(req.getContent());
        t.setAssigneeId(req.getAssigneeId());
        t.setAssignBy(currentUserId);
        t.setStatus("TODO");
        t.setCreateTime(LocalDateTime.now());
        t.setUpdateTime(LocalDateTime.now());
        taskMapper.insert(t);
        return t.getId();
    }

    @Override
    public void update(Long taskId, TaskUpdateRequest req, Long currentUserId) {
        CrmOppTask t = taskMapper.selectById(taskId);
        if (t == null) throw new IllegalArgumentException("任务不存在: " + taskId);
        if (t.getCloseTime() != null || "DONE".equals(t.getStatus())) {
            throw new IllegalArgumentException("任务已关闭,不可修改");
        }
        if (!isIronTriangle(t.getOppId(), currentUserId)) {
            throw new IllegalArgumentException("仅铁三角可修改任务");
        }
        if (req.getTaskName() != null) t.setTaskName(req.getTaskName());
        if (req.getContent() != null) t.setContent(req.getContent());
        if (req.getAssigneeId() != null) t.setAssigneeId(req.getAssigneeId());
        if (req.getStageId() != null) t.setStageId(req.getStageId());
        t.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(t);
    }

    @Override
    public void reply(Long taskId, String replyContent, Long currentUserId) {
        CrmOppTask t = taskMapper.selectById(taskId);
        if (t == null) throw new IllegalArgumentException("任务不存在: " + taskId);
        if (t.getCloseTime() != null || "DONE".equals(t.getStatus())) {
            throw new IllegalArgumentException("任务已关闭,不可回复");
        }
        boolean isAssignee = Objects.equals(t.getAssigneeId(), currentUserId);
        boolean isIron = isIronTriangle(t.getOppId(), currentUserId);
        if (!isAssignee && !isIron) {
            throw new IllegalArgumentException("仅受理人或铁三角可回复任务");
        }
        t.setReplyContent(replyContent);
        if ("TODO".equals(t.getStatus())) {
            t.setStatus("DOING");
        }
        t.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(t);
    }

    @Override
    public void close(Long taskId, Long currentUserId) {
        CrmOppTask t = taskMapper.selectById(taskId);
        if (t == null) throw new IllegalArgumentException("任务不存在: " + taskId);
        if (t.getCloseTime() != null || "DONE".equals(t.getStatus())) {
            throw new IllegalArgumentException("任务已关闭");
        }
        if (!isIronTriangle(t.getOppId(), currentUserId)) {
            throw new IllegalArgumentException("仅铁三角可关闭任务");
        }
        t.setStatus("DONE");
        t.setProgress(100);
        t.setCloseTime(LocalDateTime.now());
        t.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(t);
    }

    @Override
    public void updateProgress(Long taskId, int progress, Long currentUserId) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("进度必须在 0~100 之间");
        }
        CrmOppTask t = taskMapper.selectById(taskId);
        if (t == null) throw new IllegalArgumentException("任务不存在: " + taskId);
        if (t.getCloseTime() != null || "DONE".equals(t.getStatus())) {
            throw new IllegalArgumentException("任务已关闭,不可修改进度");
        }
        boolean isAssignee = Objects.equals(t.getAssigneeId(), currentUserId);
        boolean isIron = isIronTriangle(t.getOppId(), currentUserId);
        if (!isAssignee && !isIron) {
            throw new IllegalArgumentException("仅受理人或铁三角可修改任务进度");
        }
        t.setProgress(progress);
        // progress > 0 时,自动从 TODO 推进到 DOING(避免 progress 80% 但仍是 TODO 的违和状态)
        if (progress > 0 && "TODO".equals(t.getStatus())) {
            t.setStatus("DOING");
        }
        t.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(t);
    }

    @Override
    public boolean isIronTriangle(Long oppId, Long userId) {
        if (oppId == null || userId == null) return false;
        CrmOpportunity opp = opportunityMapper.selectById(oppId);
        if (opp == null) return false;
        return Objects.equals(opp.getPmId(), userId)
                || Objects.equals(opp.getSmId(), userId)
                || Objects.equals(opp.getDmId(), userId)
                || Objects.equals(opp.getManagerId(), userId);
    }
}
