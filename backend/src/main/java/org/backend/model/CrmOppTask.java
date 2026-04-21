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
    private String taskName;
    private String content;
    private Long assigneeId;
    private String replyContent;
    private Integer status; // 0待办，1完成
    private LocalDateTime createTime;
}
