package org.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.backend.model.CrmCustomerVisit;
import org.backend.model.Dto.visit.VisitListItemDto;

import java.util.List;

/**
 * 客户走访 Mapper
 * <p>本轮(2026/04/30)从手写 SQL 重写为 BaseMapper,补 2 个 join 查询:
 * <ul>
 *   <li>{@link #findActiveByCustomerId} 用于派单前重复性校验</li>
 *   <li>{@link #selectListWithJoins} / {@link #findByIdWithJoins} 用于走访清单/详情(含客户名/经理名)</li>
 * </ul>
 */
@Mapper
public interface CustomerVisitMapper extends BaseMapper<CrmCustomerVisit> {

    /**
     * 查找指定客户的当前活跃走访记录(派单不可重复,理论上至多一条)
     */
    @Select("SELECT * FROM crm_customer_visit WHERE customer_id = #{customerId} ORDER BY id DESC LIMIT 1")
    CrmCustomerVisit findActiveByCustomerId(@Param("customerId") Long customerId);

    /**
     * 走访清单(含客户名、客户经理姓名、派单人姓名)
     * 按 managerId / status 过滤
     */
    @Select("<script>" +
            "SELECT v.id, v.customer_id, v.manager_id, v.assign_by, v.visit_record, v.status, " +
            "       v.assign_time, v.visit_time, v.create_time, v.update_time, " +
            "       c.customer_name AS customer_name, " +
            "       c.city AS customer_city, " +
            "       c.legal_person AS customer_legal_person, " +
            "       m.real_name AS manager_name, " +
            "       ab.real_name AS assign_by_name " +
            "FROM crm_customer_visit v " +
            "LEFT JOIN crm_customer c ON c.id = v.customer_id " +
            "LEFT JOIN sys_user m ON m.id = v.manager_id " +
            "LEFT JOIN sys_user ab ON ab.id = v.assign_by " +
            "<where>" +
            "  <if test='managerId != null'> AND v.manager_id = #{managerId} </if>" +
            "  <if test=\"status != null and status != ''\"> AND v.status = #{status} </if>" +
            "</where>" +
            "ORDER BY v.create_time DESC" +
            "</script>")
    List<VisitListItemDto> selectListWithJoins(@Param("managerId") Long managerId,
                                                @Param("status") String status);

    /**
     * 单条走访详情(同 selectListWithJoins 但按 id 取一条)
     */
    @Select("SELECT v.id, v.customer_id, v.manager_id, v.assign_by, v.visit_record, v.status, " +
            "       v.assign_time, v.visit_time, v.create_time, v.update_time, " +
            "       c.customer_name AS customer_name, " +
            "       c.city AS customer_city, " +
            "       c.legal_person AS customer_legal_person, " +
            "       m.real_name AS manager_name, " +
            "       ab.real_name AS assign_by_name " +
            "FROM crm_customer_visit v " +
            "LEFT JOIN crm_customer c ON c.id = v.customer_id " +
            "LEFT JOIN sys_user m ON m.id = v.manager_id " +
            "LEFT JOIN sys_user ab ON ab.id = v.assign_by " +
            "WHERE v.id = #{id}")
    VisitListItemDto findByIdWithJoins(@Param("id") Long id);
}
