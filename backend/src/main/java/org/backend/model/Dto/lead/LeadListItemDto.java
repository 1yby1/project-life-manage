package org.backend.model.Dto.lead;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 线索清单 row(含 join 字段)
 * <p>培育详细信息(progress_desc 的 JSON)在列表中不解析,详情接口 LeadDetailDto 才解析
 */
@Data
public class LeadListItemDto {
    private Long id;
    private String title;
    /** ENTRY / COLLECTED / DISTRIBUTED / CONVERTED (本轮不用 NURTURING) */
    private String status;

    private Long customerId;
    private String customerName;
    private String customerCity;

    /** 签约概率 0-100,前端 *0.01 转 0-1 */
    private BigDecimal winRate;
    /** 线索描述 */
    private String requirement;
    private String projectScale;
    /** 培育详情 JSON 原文(列表不解析,前端按需 parse) */
    private String progressDesc;
    private String bu;

    private Long entryBy;
    private String entryByName;
    private Long collectorBy;
    private String collectorByName;
    private Long distributorBy;
    private String distributorByName;
    private Long managerId;
    private String managerName;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
