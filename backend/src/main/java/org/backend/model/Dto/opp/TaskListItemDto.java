package org.backend.model.Dto.opp;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商机任务列表 row(crm_opp_task) + join 出的姓名
 */
@Data
public class TaskListItemDto {
    private Long id;
    private Long oppId;
    private Long stageId;
    private String stageName;
    private String taskName;
    private String content;
    private Long assigneeId;
    private String assigneeName;
    private Long assignBy;
    private String assignByName;
    private String replyContent;
    /** TODO / DOING / DONE */
    private String status;
    /** 完成进度 0-100 */
    private Integer progress;
    private LocalDateTime closeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
