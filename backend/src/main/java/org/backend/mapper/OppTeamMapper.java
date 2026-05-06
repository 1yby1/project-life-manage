package org.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.backend.model.CrmOppTeam;
import org.backend.model.Dto.opp.TeamMemberDto;

import java.util.List;

@Mapper
public interface OppTeamMapper extends BaseMapper<CrmOppTeam> {

    /** 商机组员列表(含 user/添加人 姓名 + 用户岗位提示) */
    @Select("SELECT t.*, " +
            "  u.real_name  AS user_name, " +
            "  u.username   AS username, " +
            "  ab.real_name AS add_by_name " +
            "FROM crm_opp_team t " +
            "LEFT JOIN sys_user u  ON u.id  = t.user_id " +
            "LEFT JOIN sys_user ab ON ab.id = t.add_by " +
            "WHERE t.opp_id = #{oppId} " +
            "ORDER BY t.member_type, t.create_time")
    List<TeamMemberDto> selectByOppIdWithJoins(@Param("oppId") Long oppId);
}
