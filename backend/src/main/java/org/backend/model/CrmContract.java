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
    /** 合同名称(不可重复) */
    private String contractName;
    private Long customerId;
    /** 来源商机 ID */
    private Long oppId;
    /** 合同类型(下拉,可写死) */
    private String contractType;
    /** 合同金额,须等于付款节点总和 */
    private BigDecimal totalAmount;
    /** EXECUTING 执行中 / COMPLETED 已交付 / CLOSED 已关闭 */
    private String status;
    /** 合同年份,首页按年筛选 */
    private Integer contractYear;
    /** 合同正文附件 */
    private String fileUrl;
    /** 验收/交付时间(PMO 专题用) */
    private LocalDateTime deliveryTime;
    /** 关闭时间 */
    private LocalDateTime closeTime;
    /** 关闭人 */
    private Long closeBy;
    /** 创建人 */
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

