package org.backend.model.Dto.visit;

import lombok.Data;

/**
 * 走访记录写入 - PUT /customer-visits/:id/record 或 POST /customer-visits/:id/complete
 * <p>visitRecord 为 JSON 序列化的字符串,前端约定字段:
 * {startAt, endAt, contactMethod, host, address, minutes, result}
 */
@Data
public class VisitRecordRequest {
    /** JSON 字符串形式的走访记录 */
    private String visitRecord;
}
