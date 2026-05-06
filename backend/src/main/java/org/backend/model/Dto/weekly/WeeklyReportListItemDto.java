package org.backend.model.Dto.weekly;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.backend.model.CrmWeeklyReport;

@Data
@EqualsAndHashCode(callSuper = false)
public class WeeklyReportListItemDto extends CrmWeeklyReport {
    private String userName;
    private String supervisorName;
    private Integer commentCount;
}
