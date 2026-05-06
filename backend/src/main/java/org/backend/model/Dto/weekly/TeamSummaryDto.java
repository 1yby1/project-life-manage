package org.backend.model.Dto.weekly;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 主管 dashboard 周报聚合摘要 — 某周下属提交/点评统计 + 未提交名单
 */
@Data
public class TeamSummaryDto {
    private Integer year;
    private Integer weekNum;
    /** 下属总数 */
    private Integer totalCount;
    /** 已提交人数(SUBMITTED + COMMENTED) */
    private Integer submittedCount;
    /** 已点评人数(COMMENTED) */
    private Integer commentedCount;
    /** 每个下属的本周状态 */
    private List<Member> members;

    @Data
    public static class Member {
        private Long userId;
        private String userName;
        /** null 表示未提交;否则是 DRAFT/SUBMITTED/COMMENTED */
        private String status;
        private Long reportId;
        private LocalDateTime submitTime;
    }
}
