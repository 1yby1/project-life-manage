package org.backend.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("crm_customer_visit")
public class CrmCustomerVisit {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long customerId;
    /** 被派单的客户经理 */
    private Long managerId;
    /** 派单人(商机管理员) */
    private Long assignBy;
    /** 走访记录,走访完成前可多次更新 */
    private String visitRecord;
    /** PENDING 待走访 / DOING 走访中 / COMPLETED 已完成(不可逆) */
    private String status;
    /** 派单时间 */
    private LocalDateTime assignTime;
    /** 完成走访时间 */
    private LocalDateTime visitTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
