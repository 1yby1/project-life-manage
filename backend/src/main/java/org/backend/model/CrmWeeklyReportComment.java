package org.backend.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 周报点评(主管对周报发表的点评)
 */
@Data
@TableName("crm_weekly_report_comment")
public class CrmWeeklyReportComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reportId;
    /** 点评人(主管) */
    private Long commenterId;
    /** 点评内容 */
    private String content;
    private LocalDateTime createTime;
}
