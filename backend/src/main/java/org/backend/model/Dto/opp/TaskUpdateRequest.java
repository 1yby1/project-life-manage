package org.backend.model.Dto.opp;

import lombok.Data;

@Data
public class TaskUpdateRequest {
    private String taskName;
    private String content;
    private Long stageId;
    private Long assigneeId;
}
