package org.backend.model.Dto.weekly;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WeeklyReportCommentDto {
    private Long id;
    private Long reportId;
    private Long commenterId;
    private String commenterName;
    private String content;
    private LocalDateTime createTime;
}
