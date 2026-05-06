package org.backend.model.Dto.opp;

import lombok.Data;

@Data
public class TemplateStageDto {
    private Long id;
    private String stageCode;
    private String stageName;
    private Integer sortOrder;
    /** 是否必含环节 */
    private Boolean required;
}
