package org.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.backend.model.CrmOppStage;
import org.backend.model.Dto.opp.OppStageDto;

import java.util.List;

@Mapper
public interface OppStageMapper extends BaseMapper<CrmOppStage> {

    /** 商机环节实例 + 责任人姓名 */
    @Select("SELECT s.*, u.real_name AS owner_name " +
            "FROM crm_opp_stage s " +
            "LEFT JOIN sys_user u ON u.id = s.owner_id " +
            "WHERE s.opp_id = #{oppId} " +
            "ORDER BY s.sort_order")
    List<OppStageDto> selectByOppIdWithOwnerName(@Param("oppId") Long oppId);
}
