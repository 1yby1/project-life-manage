package org.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.backend.model.CrmOppTask;
import org.backend.model.Dto.opp.TaskListItemDto;

import java.util.List;

@Mapper
public interface OppTaskMapper extends BaseMapper<CrmOppTask> {

    /** 任务列表(含受理人/派发人/环节名 join) */
    @Select("<script>" +
            "SELECT t.*, " +
            "  ass.real_name AS assignee_name, " +
            "  asg.real_name AS assign_by_name, " +
            "  st.stage_name AS stage_name " +
            "FROM crm_opp_task t " +
            "LEFT JOIN sys_user ass ON ass.id = t.assignee_id " +
            "LEFT JOIN sys_user asg ON asg.id = t.assign_by " +
            "LEFT JOIN crm_opp_stage st ON st.id = t.stage_id " +
            "<where>" +
            "  <if test='oppId != null'> AND t.opp_id = #{oppId} </if>" +
            "  <if test='stageId != null'> AND t.stage_id = #{stageId} </if>" +
            "</where>" +
            "ORDER BY t.create_time DESC" +
            "</script>")
    List<TaskListItemDto> selectListWithJoins(
            @Param("oppId") Long oppId,
            @Param("stageId") Long stageId);
}
