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
    private Long managerId;
    private String visitRecord;
    private Integer status; // 0待走访，1已完成
    private LocalDateTime visitTime;
    private LocalDateTime createTime;
}
