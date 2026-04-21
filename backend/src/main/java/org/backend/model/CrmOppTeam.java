package org.backend.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("crm_opp_team")
public class CrmOppTeam {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long oppId;
    private Long userId;
    private String memberType; // CORE, SUPPORT
}
