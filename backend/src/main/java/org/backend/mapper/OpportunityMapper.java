package org.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.backend.model.CrmOpportunity;
import org.backend.model.Dto.opp.OppListItemDto;

import java.util.List;

@Mapper
public interface OpportunityMapper extends BaseMapper<CrmOpportunity> {

    /**
     * 商机列表 / 详情(传 id 取单条) — 含 customer/pm/sm/dm/manager/template 名称
     */
    @Select("<script>" +
            "SELECT o.*, " +
            "  c.customer_name AS customer_name, " +
            "  pm.real_name    AS pm_name, " +
            "  sm.real_name    AS sm_name, " +
            "  dm.real_name    AS dm_name, " +
            "  mgr.real_name   AS manager_name, " +
            "  t.template_name AS template_name " +
            "FROM crm_opportunity o " +
            "LEFT JOIN crm_customer    c   ON c.id   = o.customer_id " +
            "LEFT JOIN sys_user        pm  ON pm.id  = o.pm_id " +
            "LEFT JOIN sys_user        sm  ON sm.id  = o.sm_id " +
            "LEFT JOIN sys_user        dm  ON dm.id  = o.dm_id " +
            "LEFT JOIN sys_user        mgr ON mgr.id = o.manager_id " +
            "LEFT JOIN crm_opp_template t  ON t.id   = o.template_id " +
            "<where>" +
            "  <if test='id != null'> AND o.id = #{id} </if>" +
            "  <if test='keyword != null and keyword != \"\"'> AND o.opp_name LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "  <if test='customerId != null'> AND o.customer_id = #{customerId} </if>" +
            "  <if test='stage != null and stage != \"\"'> AND o.stage = #{stage} </if>" +
            "</where>" +
            "ORDER BY o.create_time DESC" +
            "</script>")
    List<OppListItemDto> selectListWithJoins(
            @Param("id") Long id,
            @Param("keyword") String keyword,
            @Param("customerId") Long customerId,
            @Param("stage") String stage);
}
