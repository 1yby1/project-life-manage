package org.backend.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("crm_opp_task")
public class CrmOppTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long oppId;
    /** 所属环节 ID */
    private Long stageId;
    private String taskName;
    private String content;
    /** 被指派人 */
    private Long assigneeId;
    /** 指派人 */
    private Long assignBy;
    /** 回复内容,关闭前可多次修改 */
    private String replyContent;
    /** TODO 待办 / DOING 进行中 / DONE 已关闭(不可改) */
    private String status;
    /** 完成进度 0-100;受理人 / 铁三角可改;关闭(DONE) 时强制 100,之后不可改 */
    private Integer progress;
    /** 关闭时间 */
    private LocalDateTime closeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
