package org.backend.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商机推进模板的环节定义
 */
@Data
@TableName("crm_opp_template_stage")
public class CrmOppTemplateStage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long templateId;
    /** 环节编码: VALIDATE / NEGOTIATE / IMPLEMENT / DELIVERY 或自定义 */
    private String stageCode;
    /** 环节名称 */
    private String stageName;
    /** 顺序 */
    private Integer sortOrder;
    /** 是否必含环节 0 否 / 1 是(验证/谈判/实施/交付为必含) */
    private Integer required;
}
