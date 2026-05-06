package org.backend.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 销售周报
 * 唯一约束 (user_id, year, week_num)
 */
@Data
@TableName("crm_weekly_report")
public class CrmWeeklyReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 提交人(销售) */
    private Long userId;
    /** 接收主管(冗余,提升查询效率) */
    private Long supervisorId;
    /** 年份 */
    private Integer year;
    /** 第几周(ISO 周数) */
    private Integer weekNum;
    /** 出勤情况,JSON 存每天一条 */
    private String attendance;
    /** 本周工作情况 */
    private String thisWeekWork;
    /** 下周计划 */
    private String nextWeekPlan;
    /** DRAFT 草稿 / SUBMITTED 已提交 / COMMENTED 已点评 */
    private String status;
    /** 提交时间 */
    private LocalDateTime submitTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
