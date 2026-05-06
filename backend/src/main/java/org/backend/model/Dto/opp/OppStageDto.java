package org.backend.model.Dto.opp;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商机环节实例(crm_opp_stage) + join 出的 ownerName
 */
@Data
public class OppStageDto {
    private Long id;
    private Long oppId;
    private String stageCode;
    private String stageName;
    private Integer sortOrder;
    private Long ownerId;
    private String ownerName;
    /** PENDING / DOING / DONE */
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
