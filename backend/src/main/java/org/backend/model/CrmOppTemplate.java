package org.backend.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商机推进模板
 */
@Data
@TableName("crm_opp_template")
public class CrmOppTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 模板名称 */
    private String templateName;
    /** 模板说明 */
    private String description;
    /** 是否默认模板 0 否 / 1 是 */
    private Integer isDefault;
    private LocalDateTime createTime;
}
