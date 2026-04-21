package org.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.backend.model.CrmContractPayment;
import java.util.List;

@Mapper
public interface ContractPaymentMapper {
    @Insert("INSERT INTO crm_contract_payment(contract_id, node_name, plan_amount, status) " +
            "VALUES(#{contractId}, #{nodeName}, #{planAmount}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrmContractPayment payment);

    @Select("SELECT * FROM crm_contract_payment WHERE contract_id = #{contractId}")
    List<CrmContractPayment> findByContractId(Long contractId);

    @Update("UPDATE crm_contract_payment SET status=#{status} WHERE id=#{id}")
    int updateStatus(CrmContractPayment payment);

    @Select("SELECT SUM(plan_amount) FROM crm_contract_payment WHERE contract_id = #{contractId}")
    java.math.BigDecimal getTotalPlanAmountByContractId(Long contractId);
}
