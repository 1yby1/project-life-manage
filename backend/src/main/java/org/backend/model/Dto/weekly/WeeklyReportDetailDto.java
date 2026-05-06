package org.backend.model.Dto.weekly;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class WeeklyReportDetailDto extends WeeklyReportListItemDto {
    private List<WeeklyReportCommentDto> comments;
}
