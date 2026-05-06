package org.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.backend.model.CrmContract;
import org.backend.model.Dto.contract.ContractListItemDto;

import java.util.List;

@Mapper
public interface ContractMapper extends BaseMapper<CrmContract> {

    /**
     * 合同列表(含 customer/creator/closer 名称 + 已付金额聚合)
     * <p>查询参数 id 可定位单条详情(供 detail 复用)
     */
    @Select("<script>" +
            "SELECT c.*, " +
            "  cu.customer_name AS customer_name, " +
            "  cb.real_name     AS creator_name, " +
            "  cl.real_name     AS closer_name, " +
            "  o.opp_name       AS opp_name, " +
            "  COALESCE((SELECT SUM(p.actual_amount) FROM crm_contract_payment p " +
            "            WHERE p.contract_id = c.id AND p.status = 1), 0) AS paid_amount " +
            "FROM crm_contract c " +
            "LEFT JOIN crm_customer    cu ON cu.id = c.customer_id " +
            "LEFT JOIN sys_user        cb ON cb.id = c.create_by " +
            "LEFT JOIN sys_user        cl ON cl.id = c.close_by " +
            "LEFT JOIN crm_opportunity o  ON o.id  = c.opp_id " +
            "<where>" +
            "  <if test='id != null'> AND c.id = #{id} </if>" +
            "  <if test='keyword != null and keyword != \"\"'> AND c.contract_name LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "  <if test='customerName != null and customerName != \"\"'> AND cu.customer_name LIKE CONCAT('%', #{customerName}, '%') </if>" +
            "  <if test='status != null and status != \"\"'> AND c.status = #{status} </if>" +
            "  <if test='year != null'> AND c.contract_year = #{year} </if>" +
            "  <if test='bu != null and bu != \"\"'> AND cu.bu = #{bu} </if>" +
            "</where>" +
            "ORDER BY c.create_time DESC" +
            "</script>")
    List<ContractListItemDto> selectListWithJoins(
            @Param("id") Long id,
            @Param("keyword") String keyword,
            @Param("customerName") String customerName,
            @Param("status") String status,
            @Param("year") Integer year,
            @Param("bu") String bu);
}
