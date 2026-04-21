package org.backend.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("crm_lead")
public class CrmLead {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String status; // ENTRY, COLLECTED, DISTRIBUTED, NURTURING, CONVERTED
    private Long customerId;
    private BigDecimal winRate;
    private String requirement;
    private String projectScale;
    private String progressDesc;
    private Long entryBy;
    private Long collectorBy;
    private Long distributorBy;
    private Long managerId;
    private LocalDateTime createTime;
}

