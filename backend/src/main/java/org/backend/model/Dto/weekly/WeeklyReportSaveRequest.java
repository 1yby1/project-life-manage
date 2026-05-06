package org.backend.model.Dto.weekly;

import lombok.Data;

@Data
public class WeeklyReportSaveRequest {
    /** 年份(必填) */
    private Integer year;
    /** ISO 周数(必填) */
    private Integer weekNum;
    /** 出勤情况(JSON 文本,前端序列化每天一条) */
    private String attendance;
    /** 本周工作情况 */
    private String thisWeekWork;
    /** 下周计划 */
    private String nextWeekPlan;
}
