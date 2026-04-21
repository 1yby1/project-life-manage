package org.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.backend.model.CrmCustomer;
import java.util.List;

@Mapper
public interface CustomerMapper {
    @Insert("INSERT INTO crm_customer(customer_name, city, legal_person, address, reg_address, reg_agency, " +
            "credit_code, industry, contact_name, contact_phone, contact_title, create_by) " +
            "VALUES(#{customerName}, #{city}, #{legalPerson}, #{address}, #{regAddress}, #{regAgency}, " +
            "#{creditCode}, #{industry}, #{contactName}, #{contactPhone}, #{contactTitle}, #{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrmCustomer customer);

    @Select("SELECT * FROM crm_customer WHERE id = #{id}")
    CrmCustomer findById(Long id);

    @Select("SELECT * FROM crm_customer ORDER BY create_time DESC")
    List<CrmCustomer> findAll();

    @Update("UPDATE crm_customer SET customer_name=#{customerName}, city=#{city}, legal_person=#{legalPerson}, " +
            "address=#{address}, reg_address=#{regAddress}, reg_agency=#{regAgency}, credit_code=#{creditCode}, " +
            "industry=#{industry}, contact_name=#{contactName}, contact_phone=#{contactPhone}, " +
            "contact_title=#{contactTitle} WHERE id=#{id}")
    int update(CrmCustomer customer);
}

