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
    /** ENTRY 录入 / COLLECTED 收集完成 / DISTRIBUTED 已分发 / NURTURING 培育中 / CONVERTED 已转商机 */
    private String status;
    private Long customerId;
    /** 签约概率 0-100 */
    private BigDecimal winRate;
    private String requirement;
    private String projectScale;
    private String progressDesc;
    /** 所属 BU,清单可按 BU 过滤 */
    private String bu;
    private Long entryBy;
    private Long collectorBy;
    private Long distributorBy;
    /** 被指派的客户经理 */
    private Long managerId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

