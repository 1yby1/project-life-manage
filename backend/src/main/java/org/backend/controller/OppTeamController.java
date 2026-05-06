package org.backend.controller;

import org.backend.model.CustomUserDetails;
import org.backend.model.Dto.opp.TeamMemberAddRequest;
import org.backend.model.Dto.opp.TeamMemberDto;
import org.backend.service.OppTeamService;
import org.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商机组员管理(核心组 / 支撑组)
 * <p>权限: CORE 仅 PM(opp.pm_id) 可改;SUPPORT 铁三角(PM/SM/DM/manager) 任一可改
 */
@RestController
@RequestMapping("/opp-team")
public class OppTeamController {

    @Autowired
    private OppTeamService teamService;

    /** 列表(任何登录用户) */
    @GetMapping
    public Result<List<TeamMemberDto>> list(@RequestParam Long oppId) {
        return Result.success(teamService.list(oppId));
    }

    /** 添加组员 */
    @PostMapping
    public Result<Long> add(@RequestBody TeamMemberAddRequest req,
                            @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            Long id = teamService.add(req, currentUser != null ? currentUser.getUserId() : null);
            return Result.success(id);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /** 移除组员 */
    @DeleteMapping("/{id}")
    public Result<String> remove(@PathVariable Long id,
                                 @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            teamService.remove(id, currentUser != null ? currentUser.getUserId() : null);
            return Result.success("已移除");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
