package org.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.backend.model.CrmOppTeam;
import java.util.List;

@Mapper
public interface OppTeamMapper {
    @Insert("INSERT INTO crm_opp_team(opp_id, user_id, member_type) VALUES(#{oppId}, #{userId}, #{memberType})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrmOppTeam team);

    @Select("SELECT * FROM crm_opp_team WHERE opp_id = #{oppId}")
    List<CrmOppTeam> findByOppId(Long oppId);

    @Delete("DELETE FROM crm_opp_team WHERE id = #{id}")
    int delete(Long id);

    @Select("SELECT u.real_name, t.* FROM crm_opp_team t JOIN sys_user u ON t.user_id = u.id WHERE t.opp_id = #{oppId}")
    List<CrmOppTeam> findTeamWithNames(Long oppId);
}
