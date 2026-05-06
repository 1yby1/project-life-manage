package org.backend.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商机环节实例(按模板展开)
 */
@Data
@TableName("crm_opp_stage")
public class CrmOppStage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long oppId;
    private String stageCode;
    private String stageName;
    private Integer sortOrder;
    /** 环节负责人,空则默认为项目经理 */
    private Long ownerId;
    /** PENDING / DOING / DONE */
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
