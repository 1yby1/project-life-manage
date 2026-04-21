package org.backend.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("crm_customer")
public class CrmCustomer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String customerName;
    private String city;
    private String legalPerson;
    private String address;
    private String regAddress;
    private String regAgency;
    private String creditCode;
    private String industry;
    private String contactName;
    private String contactPhone;
    private String contactTitle;
    private Long createBy;
    private LocalDateTime createTime;
}

