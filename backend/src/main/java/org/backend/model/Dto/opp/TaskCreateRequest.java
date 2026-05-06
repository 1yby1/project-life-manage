package org.backend.model.Dto.opp;

import lombok.Data;

@Data
public class TaskCreateRequest {
    private Long oppId;
    private Long stageId;
    private String taskName;
    private String content;
    /** 受理人 (允许空,创建后通过 update 再指派) */
    private Long assigneeId;
}
