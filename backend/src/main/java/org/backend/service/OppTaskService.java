package org.backend.service;

import org.backend.model.Dto.opp.TaskCreateRequest;
import org.backend.model.Dto.opp.TaskListItemDto;
import org.backend.model.Dto.opp.TaskUpdateRequest;

import java.util.List;

public interface OppTaskService {

    /** 任务列表(过滤 oppId / stageId 任一可选) */
    List<TaskListItemDto> list(Long oppId, Long stageId);

    /** 创建任务(仅商机的铁三角:PM/SM/DM/manager) */
    Long create(TaskCreateRequest req, Long currentUserId);

    /** 更新任务(铁三角 + 任务未关闭) */
    void update(Long taskId, TaskUpdateRequest req, Long currentUserId);

    /** 任务回复(受理人或铁三角 + 未关闭) */
    void reply(Long taskId, String replyContent, Long currentUserId);

    /**
     * 更新任务进度 0-100
     * <p>权限: 受理人(assignee_id) 或 铁三角(PM/SM/DM/manager) + 任务未关闭
     * <p>关闭(DONE)的任务拒绝改 progress
     */
    void updateProgress(Long taskId, int progress, Long currentUserId);

    /** 关闭任务(铁三角 + 未关闭) */
    void close(Long taskId, Long currentUserId);

    /** 是否商机的铁三角(PM/SM/DM/manager) */
    boolean isIronTriangle(Long oppId, Long userId);
}
