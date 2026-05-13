package org.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.backend.model.CrmContract;
import org.backend.model.Dto.contract.ContractDimensionAggregate;
import org.backend.model.Dto.contract.ContractListItemDto;

import java.util.List;
import java.util.Map;

@Mapper
public interface ContractMapper extends BaseMapper<CrmContract> {

    /**
     * 合同列表(含 customer/creator/closer 名称 + 已付金额聚合)
     * <p>查询参数 id 可定位单条详情(供 detail 复用);不分页,适合专题查询(in-flight / accepted)与 detail。
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

    /**
     * 分页查询合同列表(交由 MybatisPlus 分页拦截器自动追加 LIMIT 与 COUNT)
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
            "  <if test='keyword != null and keyword != \"\"'> AND c.contract_name LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "  <if test='customerName != null and customerName != \"\"'> AND cu.customer_name LIKE CONCAT('%', #{customerName}, '%') </if>" +
            "  <if test='status != null and status != \"\"'> AND c.status = #{status} </if>" +
            "  <if test='year != null'> AND c.contract_year = #{year} </if>" +
            "  <if test='bu != null and bu != \"\"'> AND cu.bu = #{bu} </if>" +
            "</where>" +
            "ORDER BY c.create_time DESC" +
            "</script>")
    IPage<ContractListItemDto> selectPageWithJoins(
            Page<ContractListItemDto> page,
            @Param("keyword") String keyword,
            @Param("customerName") String customerName,
            @Param("status") String status,
            @Param("year") Integer year,
            @Param("bu") String bu);

    /**
     * 专题汇总(同筛选条件下的全量合同金额与已收款合计)
     * <p>用于 PMO 已验收 / 区总在途专题分页时,KPI 卡片显示全量汇总而非当前页。
     * <p>返回字段:totalAmountSum(BigDecimal), totalPaidSum(BigDecimal)。
     */
    @Select("<script>" +
            "SELECT " +
            "  COALESCE(SUM(c.total_amount), 0) AS totalAmountSum, " +
            "  COALESCE(SUM((SELECT COALESCE(SUM(p.actual_amount), 0) FROM crm_contract_payment p " +
            "                WHERE p.contract_id = c.id AND p.status = 1)), 0) AS totalPaidSum " +
            "FROM crm_contract c " +
            "LEFT JOIN crm_customer cu ON cu.id = c.customer_id " +
            "<where>" +
            "  <if test='status != null and status != \"\"'> AND c.status = #{status} </if>" +
            "  <if test='year != null'> AND c.contract_year = #{year} </if>" +
            "  <if test='bu != null and bu != \"\"'> AND cu.bu = #{bu} </if>" +
            "</where>" +
            "</script>")
    Map<String, Object> selectTopicAggregates(
            @Param("status") String status,
            @Param("year") Integer year,
            @Param("bu") String bu);

    /**
     * 按年度聚合(dimension = contract_year as string)
     * <p>用于已验收专题的"按年度合同金额 / 项目数"柱状图。
     */
    @Select("<script>" +
            "SELECT " +
            "  CAST(c.contract_year AS CHAR) AS dimension, " +
            "  COUNT(*) AS count, " +
            "  COALESCE(SUM(c.total_amount), 0) AS totalAmount, " +
            "  COALESCE(SUM((SELECT COALESCE(SUM(p.actual_amount), 0) FROM crm_contract_payment p " +
            "                WHERE p.contract_id = c.id AND p.status = 1)), 0) AS totalPaid " +
            "FROM crm_contract c " +
            "LEFT JOIN crm_customer cu ON cu.id = c.customer_id " +
            "<where>" +
            "  <if test='status != null and status != \"\"'> AND c.status = #{status} </if>" +
            "  <if test='bu != null and bu != \"\"'> AND cu.bu = #{bu} </if>" +
            "</where>" +
            "GROUP BY c.contract_year " +
            "ORDER BY c.contract_year ASC" +
            "</script>")
    List<ContractDimensionAggregate> aggregateByYear(
            @Param("status") String status,
            @Param("bu") String bu);

    /**
     * 按 BU 聚合(dimension = customer.bu;BU 为 NULL / 空 时归入 '未分类')
     * <p>用于在途专题的"按 BU 合同金额 vs 已收款"堆叠柱状图。
     */
    @Select("<script>" +
            "SELECT " +
            "  COALESCE(NULLIF(cu.bu, ''), '未分类') AS dimension, " +
            "  COUNT(*) AS count, " +
            "  COALESCE(SUM(c.total_amount), 0) AS totalAmount, " +
            "  COALESCE(SUM((SELECT COALESCE(SUM(p.actual_amount), 0) FROM crm_contract_payment p " +
            "                WHERE p.contract_id = c.id AND p.status = 1)), 0) AS totalPaid " +
            "FROM crm_contract c " +
            "LEFT JOIN crm_customer cu ON cu.id = c.customer_id " +
            "<where>" +
            "  <if test='status != null and status != \"\"'> AND c.status = #{status} </if>" +
            "  <if test='year != null'> AND c.contract_year = #{year} </if>" +
            "</where>" +
            "GROUP BY COALESCE(NULLIF(cu.bu, ''), '未分类') " +
            "ORDER BY totalAmount DESC"
            + "</script>")
    List<ContractDimensionAggregate> aggregateByBu(
            @Param("status") String status,
            @Param("year") Integer year);
}
