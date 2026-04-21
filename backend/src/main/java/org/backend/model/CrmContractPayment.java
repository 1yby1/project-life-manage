package org.backend.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("crm_contract_payment")
public class CrmContractPayment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long contractId;
    private String nodeName;
    private BigDecimal planAmount;
    private Integer status; // 0未付，1已付
    private LocalDateTime createTime;
}
