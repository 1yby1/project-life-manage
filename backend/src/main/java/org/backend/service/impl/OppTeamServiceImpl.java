package org.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.backend.mapper.OppTeamMapper;
import org.backend.mapper.OpportunityMapper;
import org.backend.model.CrmOppTeam;
import org.backend.model.CrmOpportunity;
import org.backend.model.Dto.opp.TeamMemberAddRequest;
import org.backend.model.Dto.opp.TeamMemberDto;
import org.backend.service.OppTeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class OppTeamServiceImpl implements OppTeamService {

    @Autowired
    private OppTeamMapper teamMapper;
    @Autowired
    private OpportunityMapper opportunityMapper;

    @Override
    public List<TeamMemberDto> list(Long oppId) {
        if (oppId == null) return List.of();
        return teamMapper.selectByOppIdWithJoins(oppId);
    }

    @Override
    public Long add(TeamMemberAddRequest req, Long currentUserId) {
        if (req.getOppId() == null) throw new IllegalArgumentException("商机 ID 必填");
        if (req.getUserId() == null) throw new IllegalArgumentException("用户 ID 必填");
        if (req.getMemberType() == null) throw new IllegalArgumentException("组别(CORE/SUPPORT)必填");
        if (!"CORE".equals(req.getMemberType()) && !"SUPPORT".equals(req.getMemberType())) {
            throw new IllegalArgumentException("组别仅支持 CORE 或 SUPPORT");
        }

        CrmOpportunity opp = opportunityMapper.selectById(req.getOppId());
        if (opp == null) throw new IllegalArgumentException("商机不存在: " + req.getOppId());

        // 权限校验
        if ("CORE".equals(req.getMemberType())) {
            if (!Objects.equals(opp.getPmId(), currentUserId)) {
                throw new IllegalArgumentException("仅项目经理可管理核心组成员(业务规则 #7)");
            }
        } else {
            if (!isIronTriangle(opp, currentUserId)) {
                throw new IllegalArgumentException("仅铁三角(PM/SM/DM/商机负责人)可管理支撑组成员");
            }
        }

        // 防重: (opp_id, user_id) 唯一
        Long dup = teamMapper.selectCount(
                new LambdaQueryWrapper<CrmOppTeam>()
                        .eq(CrmOppTeam::getOppId, req.getOppId())
                        .eq(CrmOppTeam::getUserId, req.getUserId())
        );
        if (dup != null && dup > 0) {
            throw new IllegalArgumentException("该用户已加入此商机的组,请勿重复添加");
        }

        CrmOppTeam t = new CrmOppTeam();
        t.setOppId(req.getOppId());
        t.setUserId(req.getUserId());
        t.setMemberType(req.getMemberType());
        t.setGroupName(req.getGroupName());
        t.setRole(req.getRole());
        t.setAddBy(currentUserId);
        t.setCreateTime(LocalDateTime.now());
        teamMapper.insert(t);
        return t.getId();
    }

    @Override
    public void remove(Long teamId, Long currentUserId) {
        CrmOppTeam t = teamMapper.selectById(teamId);
        if (t == null) throw new IllegalArgumentException("组员记录不存在: " + teamId);

        CrmOpportunity opp = opportunityMapper.selectById(t.getOppId());
        if (opp == null) throw new IllegalArgumentException("商机不存在");

        if ("CORE".equals(t.getMemberType())) {
            if (!Objects.equals(opp.getPmId(), currentUserId)) {
                throw new IllegalArgumentException("仅项目经理可移除核心组成员(业务规则 #7)");
            }
        } else {
            if (!isIronTriangle(opp, currentUserId)) {
                throw new IllegalArgumentException("仅铁三角可移除支撑组成员");
            }
        }

        teamMapper.deleteById(teamId);
    }

    private boolean isIronTriangle(CrmOpportunity opp, Long userId) {
        if (opp == null || userId == null) return false;
        return Objects.equals(opp.getPmId(), userId)
                || Objects.equals(opp.getSmId(), userId)
                || Objects.equals(opp.getDmId(), userId)
                || Objects.equals(opp.getManagerId(), userId);
    }
}
