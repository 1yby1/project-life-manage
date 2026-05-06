package org.backend.model.Dto.lead;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 线索培育 - PUT /leads/:id/cultivate (CUSTOMER_MANAGER 仅自己, status=DISTRIBUTED)
 * <p>winRate 直接映射 crm_lead.win_rate(0-100)。
 * 其余 7 字段作为培育详细信息序列化为 JSON 存到 progress_desc。
 */
@Data
public class LeadCultivateRequest {
    /** 签约概率(0-100,前端 0-1 *100 后传) */
    private BigDecimal winRate;

    // ========== 以下 7 字段会序列化为 JSON 存到 progress_desc ==========
    /** 项目名称 */
    private String projectName;
    /** 预计采购时间(YYYY-MM 字符串) */
    private String expectedPurchaseTime;
    /** 预测金额 */
    private BigDecimal predictedAmount;
    /** 线索等级 A/B/C */
    private String clueLevel;
    /** 解决方案经理(自由文本) */
    private String solutionManager;
    /** 商机负责人(项目经理) ID */
    private Long opportunityOwnerId;

    /** 客户关键人(嵌套对象,序列化时一起进 JSON) */
    private KeyContact keyContact;

    @Data
    public static class KeyContact {
        private String name;
        private String title;
        private String phone;
    }
}
