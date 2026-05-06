package org.backend.model.Dto.opp;

import lombok.Data;
import java.util.List;

@Data
public class OppTemplateDto {
    private Long id;
    private String templateName;
    private String description;
    private Boolean isDefault;
    private List<TemplateStageDto> stages;
}
