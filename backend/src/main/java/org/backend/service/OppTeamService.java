package org.backend.service;

import org.backend.model.Dto.opp.TeamMemberAddRequest;
import org.backend.model.Dto.opp.TeamMemberDto;

import java.util.List;

public interface OppTeamService {

    /** 商机组员列表(含 user 姓名) */
    List<TeamMemberDto> list(Long oppId);

    /**
     * 添加组员
     * <p>权限: CORE 仅 PM(opp.pm_id == currentUser);SUPPORT 铁三角(PM/SM/DM/manager) 任一
     */
    Long add(TeamMemberAddRequest req, Long currentUserId);

    /**
     * 移除组员
     * <p>权限: CORE 仅 PM;SUPPORT 铁三角任一
     */
    void remove(Long teamId, Long currentUserId);
}
