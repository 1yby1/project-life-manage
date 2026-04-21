package org.backend.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("crm_opportunity")
public class CrmOpportunity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long leadId;
    private Long customerId;
    private String oppName;
    private String stage; // VALIDATE, NEGOTIATE, IMPLEMENT, DELIVERY
    private Long managerId;
    private LocalDateTime createTime;
}

