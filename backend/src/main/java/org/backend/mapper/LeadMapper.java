package org.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.backend.model.CrmLead;
import org.backend.model.Dto.lead.LeadListItemDto;

import java.util.List;

/**
 * 线索 Mapper
 * <p>本轮(2026/05/01)从手写 SQL 重写为 BaseMapper,补 2 个 join 查询:
 * <ul>
 *   <li>{@link #selectListWithJoins} 清单(含客户名/经理名/录入人名 + filter 条件)</li>
 *   <li>{@link #findByIdWithJoins} 详情</li>
 * </ul>
 */
@Mapper
public interface LeadMapper extends BaseMapper<CrmLead> {

    /**
     * 线索清单(含客户名、客户经理姓名、录入人姓名)
     * <p>filter 取值: all(默认全部) / mine(我录入) / participate(我录入或我培育) / todo(基于 status + 当前用户角色)
     * <p>todo 的精确语义在 Service 层用 status + currentUserId 过滤,这里只做基础查询
     */
    @Select("<script>" +
            "SELECT l.id, l.title, l.status, l.customer_id, l.win_rate, l.requirement, " +
            "       l.project_scale, l.progress_desc, l.bu, l.entry_by, l.collector_by, " +
            "       l.distributor_by, l.manager_id, l.create_time, l.update_time, " +
            "       c.customer_name AS customer_name, " +
            "       c.city AS customer_city, " +
            "       eu.real_name AS entry_by_name, " +
            "       cu.real_name AS collector_by_name, " +
            "       du.real_name AS distributor_by_name, " +
            "       mu.real_name AS manager_name " +
            "FROM crm_lead l " +
            "LEFT JOIN crm_customer c ON c.id = l.customer_id " +
            "LEFT JOIN sys_user eu ON eu.id = l.entry_by " +
            "LEFT JOIN sys_user cu ON cu.id = l.collector_by " +
            "LEFT JOIN sys_user du ON du.id = l.distributor_by " +
            "LEFT JOIN sys_user mu ON mu.id = l.manager_id " +
            "<where>" +
            "  <if test=\"keyword != null and keyword != ''\">" +
            "    AND (l.title LIKE CONCAT('%', #{keyword}, '%') OR c.customer_name LIKE CONCAT('%', #{keyword}, '%'))" +
            "  </if>" +
            "  <if test=\"bu != null and bu != ''\"> AND l.bu = #{bu} </if>" +
            "  <if test=\"status != null and status != ''\"> AND l.status = #{status} </if>" +
            "  <if test=\"excludeConverted\"> AND l.status != 'CONVERTED' </if>" +
            "  <if test=\"entryBy != null\"> AND l.entry_by = #{entryBy} </if>" +
            "  <if test=\"managerId != null\"> AND l.manager_id = #{managerId} </if>" +
            "  <if test=\"participantUserId != null\">" +
            "    AND (l.entry_by = #{participantUserId} OR l.collector_by = #{participantUserId} " +
            "         OR l.distributor_by = #{participantUserId} OR l.manager_id = #{participantUserId})" +
            "  </if>" +
            "</where>" +
            "ORDER BY l.create_time DESC" +
            "</script>")
    List<LeadListItemDto> selectListWithJoins(
            @Param("keyword") String keyword,
            @Param("bu") String bu,
            @Param("status") String status,
            @Param("excludeConverted") boolean excludeConverted,
            @Param("entryBy") Long entryBy,
            @Param("managerId") Long managerId,
            @Param("participantUserId") Long participantUserId);

    /**
     * 单条详情(含 join 字段)
     */
    @Select("SELECT l.id, l.title, l.status, l.customer_id, l.win_rate, l.requirement, " +
            "       l.project_scale, l.progress_desc, l.bu, l.entry_by, l.collector_by, " +
            "       l.distributor_by, l.manager_id, l.create_time, l.update_time, " +
            "       c.customer_name AS customer_name, " +
            "       c.city AS customer_city, " +
            "       eu.real_name AS entry_by_name, " +
            "       cu.real_name AS collector_by_name, " +
            "       du.real_name AS distributor_by_name, " +
            "       mu.real_name AS manager_name " +
            "FROM crm_lead l " +
            "LEFT JOIN crm_customer c ON c.id = l.customer_id " +
            "LEFT JOIN sys_user eu ON eu.id = l.entry_by " +
            "LEFT JOIN sys_user cu ON cu.id = l.collector_by " +
            "LEFT JOIN sys_user du ON du.id = l.distributor_by " +
            "LEFT JOIN sys_user mu ON mu.id = l.manager_id " +
            "WHERE l.id = #{id}")
    LeadListItemDto findByIdWithJoins(@Param("id") Long id);
}
