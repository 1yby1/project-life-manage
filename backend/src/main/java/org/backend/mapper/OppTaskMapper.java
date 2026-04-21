package org.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.backend.model.CrmOppTask;
import java.util.List;

@Mapper
public interface OppTaskMapper {
    @Insert("INSERT INTO crm_opp_task(opp_id, task_name, content, assignee_id, status) " +
            "VALUES(#{oppId}, #{taskName}, #{content}, #{assigneeId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrmOppTask task);

    @Select("SELECT * FROM crm_opp_task WHERE opp_id = #{oppId}")
    List<CrmOppTask> findByOppId(Long oppId);

    @Update("UPDATE crm_opp_task SET reply_content=#{replyContent}, status=#{status} WHERE id=#{id}")
    int updateTaskStatus(CrmOppTask task);

    @Select("SELECT * FROM crm_opp_task WHERE assignee_id = #{assigneeId} AND status = 0")
    List<CrmOppTask> findTodoByAssigneeId(Long assigneeId);
}

