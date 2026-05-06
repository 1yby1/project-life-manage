package org.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.backend.model.CrmWeeklyReport;
import org.backend.model.Dto.weekly.WeeklyReportListItemDto;

import java.util.List;

@Mapper
public interface WeeklyReportMapper extends BaseMapper<CrmWeeklyReport> {

    /** 周报列表(含 user/supervisor 姓名 + 评论数;详情用 id 过滤;归属过滤 userId/supervisorId 二选一) */
    @Select("<script>" +
            "SELECT r.*, " +
            "  u.real_name  AS user_name, " +
            "  sv.real_name AS supervisor_name, " +
            "  (SELECT COUNT(*) FROM crm_weekly_report_comment c WHERE c.report_id = r.id) AS comment_count " +
            "FROM crm_weekly_report r " +
            "LEFT JOIN sys_user u  ON u.id  = r.user_id " +
            "LEFT JOIN sys_user sv ON sv.id = r.supervisor_id " +
            "<where>" +
            "  <if test='id != null'> AND r.id = #{id} </if>" +
            "  <if test='userId != null'> AND r.user_id = #{userId} </if>" +
            "  <if test='supervisorId != null'> AND r.supervisor_id = #{supervisorId} </if>" +
            "  <if test='status != null and status != \"\"'> AND r.status = #{status} </if>" +
            "  <if test='year != null'> AND r.year = #{year} </if>" +
            "  <if test='weekNum != null'> AND r.week_num = #{weekNum} </if>" +
            "</where>" +
            "ORDER BY r.year DESC, r.week_num DESC, r.create_time DESC" +
            "</script>")
    List<WeeklyReportListItemDto> selectListWithJoins(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("supervisorId") Long supervisorId,
            @Param("status") String status,
            @Param("year") Integer year,
            @Param("weekNum") Integer weekNum);
}
