package org.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.backend.model.CrmCustomerVisit;
import java.util.List;

@Mapper
public interface CustomerVisitMapper {
    @Insert("INSERT INTO crm_customer_visit(customer_id, manager_id, visit_record, status, visit_time) " +
            "VALUES(#{customerId}, #{managerId}, #{visitRecord}, #{status}, #{visitTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrmCustomerVisit visit);

    @Select("SELECT * FROM crm_customer_visit WHERE id = #{id}")
    CrmCustomerVisit findById(Long id);

    @Select("SELECT * FROM crm_customer_visit WHERE customer_id = #{customerId}")
    List<CrmCustomerVisit> findByCustomerId(Long customerId);

    @Update("UPDATE crm_customer_visit SET visit_record=#{visitRecord}, status=#{status}, visit_time=#{visitTime} " +
            "WHERE id=#{id} AND status = 0")
    int updateVisitRecord(CrmCustomerVisit visit);
}
