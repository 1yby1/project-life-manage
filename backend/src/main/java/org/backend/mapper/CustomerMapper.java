package org.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.backend.model.CrmCustomer;
import org.backend.model.Dto.customer.CustomerListItemDto;

@Mapper
public interface CustomerMapper extends BaseMapper<CrmCustomer> {

    /**
     * 客户列表(含派单状态派生字段)
     * <p>visitStatus 取值: PENDING/DOING/COMPLETED 走 v.status 等值;
     * "NONE"(代表"未派单") 走 v.id IS NULL;空 / null 不过滤。
     * <p>同一客户因 Service 层校验只能派一次,LEFT JOIN 不会膨胀行数。
     */
    @Select("<script>" +
            "SELECT c.id, c.customer_name, c.city, c.legal_person, " +
            "       c.address, c.reg_address, c.reg_agency, c.credit_code, " +
            "       c.industry, c.contact_name, c.contact_phone, c.contact_title, " +
            "       c.bu, c.status, c.create_by, c.create_time, c.update_time, " +
            "       v.status AS visit_status, " +
            "       v.manager_id AS assigned_manager_id, " +
            "       u.real_name AS assigned_manager_name " +
            "FROM crm_customer c " +
            "LEFT JOIN crm_customer_visit v ON v.customer_id = c.id " +
            "LEFT JOIN sys_user u ON u.id = v.manager_id " +
            "<where>" +
            "  <if test=\"keyword != null and keyword != ''\">" +
            "    AND (c.customer_name LIKE CONCAT('%', #{keyword}, '%') " +
            "         OR c.legal_person LIKE CONCAT('%', #{keyword}, '%') " +
            "         OR c.contact_name LIKE CONCAT('%', #{keyword}, '%'))" +
            "  </if>" +
            "  <if test=\"city != null and city != ''\">" +
            "    AND c.city = #{city}" +
            "  </if>" +
            "  <if test=\"visitStatus != null and visitStatus == 'NONE'\">" +
            "    AND v.id IS NULL" +
            "  </if>" +
            "  <if test=\"visitStatus != null and visitStatus != '' and visitStatus != 'NONE'\">" +
            "    AND v.status = #{visitStatus}" +
            "  </if>" +
            "</where>" +
            "ORDER BY c.create_time DESC" +
            "</script>")
    IPage<CustomerListItemDto> selectListWithVisitStatus(
            Page<CustomerListItemDto> page,
            @Param("keyword") String keyword,
            @Param("city") String city,
            @Param("visitStatus") String visitStatus);
}
