package org.backend.model.Dto.visit;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 走访清单 / 详情 DTO,含 join 出的客户名 + 客户经理姓名 + 派单人姓名
 */
@Data
public class VisitListItemDto {
    private Long id;
    private Long customerId;
    private String customerName;
    private String customerCity;
    private String customerLegalPerson;
    private Long managerId;
    private String managerName;
    private Long assignBy;
    private String assignByName;
    /** JSON 字符串形式的走访记录 */
    private String visitRecord;
    /** PENDING / DOING / COMPLETED */
    private String status;
    private LocalDateTime assignTime;
    private LocalDateTime visitTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
