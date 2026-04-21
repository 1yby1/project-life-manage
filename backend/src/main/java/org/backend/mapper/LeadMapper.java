package org.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.backend.model.CrmLead;
import java.util.List;

@Mapper
public interface LeadMapper {
    @Insert("INSERT INTO crm_lead(title, status, customer_id, win_rate, requirement, project_scale, progress_desc, entry_by, manager_id) " +
            "VALUES(#{title}, #{status}, #{customerId}, #{winRate}, #{requirement}, #{projectScale}, #{progressDesc}, #{entryBy}, #{managerId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrmLead lead);

    @Select("SELECT * FROM crm_lead WHERE id = #{id}")
    CrmLead findById(Long id);

    @Select("SELECT * FROM crm_lead ORDER BY create_time DESC")
    List<CrmLead> findAll();

    @Update("UPDATE crm_lead SET " +
            "status=#{status}, " +
            "customer_id=#{customerId}, " +
            "win_rate=#{winRate}, " +
            "requirement=#{requirement}, " +
            "project_scale=#{projectScale}, " +
            "progress_desc=#{progressDesc}, " +
            "collector_by=#{collectorBy}, " +
            "distributor_by=#{distributorBy}, " +
            "manager_id=#{managerId} " +
            "WHERE id=#{id}")
    int update(CrmLead lead);

    @Select("SELECT * FROM crm_lead WHERE manager_id = #{managerId}")
    List<CrmLead> findByManagerId(Long managerId);
}
