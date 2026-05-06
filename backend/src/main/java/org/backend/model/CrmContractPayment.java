package org.backend.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("crm_contract_payment")
public class CrmContractPayment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long contractId;
    /** 付款节点名称 */
    private String nodeName;
    /** 计划金额 */
    private BigDecimal planAmount;
    /** 实际到款 */
    private BigDecimal actualAmount;
    /** 0 未付 / 1 已付 */
    private Integer status;
    /** 计划付款日期 */
    private LocalDate planDate;
    /** 实际付款时间 */
    private LocalDateTime payTime;
    private LocalDateTime createTime;
}
