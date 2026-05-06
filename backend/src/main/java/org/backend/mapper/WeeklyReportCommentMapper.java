package org.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.backend.model.CrmWeeklyReportComment;
import org.backend.model.Dto.weekly.WeeklyReportCommentDto;

import java.util.List;

@Mapper
public interface WeeklyReportCommentMapper extends BaseMapper<CrmWeeklyReportComment> {

    /** 某周报的全部点评(含 commenter 姓名) */
    @Select("SELECT c.*, u.real_name AS commenter_name " +
            "FROM crm_weekly_report_comment c " +
            "LEFT JOIN sys_user u ON u.id = c.commenter_id " +
            "WHERE c.report_id = #{reportId} " +
            "ORDER BY c.create_time")
    List<WeeklyReportCommentDto> selectByReportId(@Param("reportId") Long reportId);
}
