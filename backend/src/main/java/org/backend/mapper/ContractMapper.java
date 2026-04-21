package org.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.backend.model.CrmContract;
import java.util.List;

@Mapper
public interface ContractMapper {
    @Insert("INSERT INTO crm_contract(contract_name, customer_id, contract_type, total_amount, status, contract_year, file_url) " +
            "VALUES(#{contractName}, #{customerId}, #{contractType}, #{totalAmount}, #{status}, #{contractYear}, #{fileUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrmContract contract);

    @Select("SELECT * FROM crm_contract WHERE id = #{id}")
    CrmContract findById(Long id);

    @Select("SELECT * FROM crm_contract WHERE contract_name = #{contractName}")
    CrmContract findByName(String contractName);

    @Select("SELECT * FROM crm_contract ORDER BY create_time DESC")
    List<CrmContract> findAll();

    @Update("UPDATE crm_contract SET status=#{status}, total_amount=#{totalAmount}, file_url=#{fileUrl} WHERE id=#{id}")
    int update(CrmContract contract);

    @Select("SELECT * FROM crm_contract WHERE contract_year = #{year}")
    List<CrmContract> findByYear(Integer year);
}

