package org.backend.model.Dto.customer;

import lombok.Data;

@Data
public class CustomerSaveRequest {
    /** 必填: 客户名称 */
    private String customerName;
    /** 必填: 地市 */
    private String city;
    /** 必填: 法人 */
    private String legalPerson;

    private String address;
    private String regAddress;
    private String regAgency;
    private String creditCode;
    private String industry;
    private String contactName;
    private String contactPhone;
    private String contactTitle;
    private String bu;
}
