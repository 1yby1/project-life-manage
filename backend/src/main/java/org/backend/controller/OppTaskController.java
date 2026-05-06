package org.backend.controller;

import org.backend.model.CustomUserDetails;
import org.backend.model.Dto.opp.TaskCreateRequest;
import org.backend.model.Dto.opp.TaskListItemDto;
import org.backend.model.Dto.opp.TaskReplyRequest;
import org.backend.model.Dto.opp.TaskUpdateRequest;
import org.backend.service.OppTaskService;
import org.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/opp-tasks")
public class OppTaskController {

    @Autowired
    private OppTaskService taskService;

    @GetMapping
    public Result<List<TaskListItemDto>> list(@RequestParam(required = false) Long oppId,
                                              @RequestParam(required = false) Long stageId) {
        return Result.success(taskService.list(oppId, stageId));
    }

    @PostMapping
    public Result<Long> create(@RequestBody TaskCreateRequest req,
                               @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            Long id = taskService.create(req, currentUser != null ? currentUser.getUserId() : null);
            return Result.success(id);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id,
                                 @RequestBody TaskUpdateRequest req,
                                 @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            taskService.update(id, req, currentUser != null ? currentUser.getUserId() : null);
            return Result.success("更新成功");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /** 任务回复 — 受理人或铁三角可调,关闭后拒绝 */
    @PostMapping("/{id}/reply")
    public Result<String> reply(@PathVariable Long id,
                                @RequestBody TaskReplyRequest req,
                                @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            taskService.reply(id, req.getReplyContent(),
                    currentUser != null ? currentUser.getUserId() : null);
            return Result.success("回复已保存");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /** 关闭任务 — 仅铁三角,关闭后不可改 */
    @PostMapping("/{id}/close")
    public Result<String> close(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            taskService.close(id, currentUser != null ? currentUser.getUserId() : null);
            return Result.success("任务已关闭");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /** 更新任务进度 0-100 — 受理人或铁三角,关闭后拒绝 */
    @PutMapping("/{id}/progress")
    public Result<String> updateProgress(@PathVariable Long id,
                                         @RequestBody java.util.Map<String, Integer> body,
                                         @AuthenticationPrincipal CustomUserDetails currentUser) {
        Integer progress = body == null ? null : body.get("progress");
        if (progress == null) return Result.error(400, "progress 字段必填");
        try {
            taskService.updateProgress(id, progress, currentUser != null ? currentUser.getUserId() : null);
            return Result.success("进度已更新");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
