package org.backend.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("crm_contract")
public class CrmContract {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String contractName;
    private Long customerId;
    private String contractType;
    private BigDecimal totalAmount;
    private String status; // EXECUTING, COMPLETED, CLOSED
    private Integer contractYear;
    private String fileUrl;
    private LocalDateTime createTime;
}

