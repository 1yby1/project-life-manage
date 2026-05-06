package org.backend.model.Dto.opp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.backend.model.CrmOpportunity;

@Data
@EqualsAndHashCode(callSuper = false)
public class OppListItemDto extends CrmOpportunity {
    /** join 自 crm_customer.customer_name */
    private String customerName;
    /** join 自 sys_user(pm_id).real_name */
    private String pmName;
    private String smName;
    private String dmName;
    /** join 自 sys_user(manager_id).real_name(商机负责人) */
    private String managerName;
    /** join 自 crm_opp_template.template_name */
    private String templateName;
}
