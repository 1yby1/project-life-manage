package org.backend.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("crm_opportunity")
public class CrmOpportunity {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 来源线索 ID */
    private Long leadId;
    private Long customerId;
    private String oppName;
    /** VALIDATE 验证 / NEGOTIATE 谈判 / IMPLEMENT 实施 / DELIVERY 交付 */
    private String stage;
    /** 使用的推进模板 ID */
    private Long templateId;
    /** 商机负责人(创建人) */
    private Long managerId;
    /** 项目经理 (铁三角) */
    private Long pmId;
    /** 解决方案经理 (铁三角) */
    private Long smId;
    /** 交付经理 (铁三角) */
    private Long dmId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

