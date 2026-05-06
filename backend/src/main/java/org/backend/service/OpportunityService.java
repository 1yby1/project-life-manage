package org.backend.service;

import org.backend.model.CrmOpportunity;
import org.backend.model.Dto.lead.LeadCultivateRequest;
import org.backend.model.Dto.opp.OppDetailDto;
import org.backend.model.Dto.opp.OppListItemDto;

import java.util.List;

/**
 * 商机服务
 * <p>包含: 占位创建(线索转商机时调用) + 列表/详情 + 选模板生成环节 + 设置环节责任人
 */
public interface OpportunityService {

    /** 从线索转商机时创建 crm_opportunity 占位记录 */
    CrmOpportunity createPlaceholderFromLead(Long leadId,
                                             Long customerId,
                                             String opportunityName,
                                             Long ownerId,
                                             LeadCultivateRequest cultivateInfo);

    /** 商机列表(支持 keyword/customerId/stage 过滤) */
    List<OppListItemDto> list(String keyword, Long customerId, String stage);

    /** 商机详情(含 stages 与 tasks) */
    OppDetailDto detail(Long oppId);

    /**
     * 选模板生成环节实例
     * <p>权限: 仅商机的 PM(opp.pm_id == currentUser)
     * <p>会先删除该商机已有的 stages,再按模板重建
     */
    void applyTemplate(Long oppId, Long templateId, Long currentUserId);

    /**
     * 设置环节责任人
     * <p>权限: PM 可改任何环节;非 PM 仅当前 owner 可改自己环节
     */
    void setStageOwner(Long stageId, Long ownerId, Long currentUserId);

    /**
     * 推进商机阶段(VALIDATE → NEGOTIATE → IMPLEMENT → DELIVERY)
     * <p>权限: 仅商机的 PM
     * <p>规则: 顺序推进,不可跳跃/倒退;DELIVERY 不可再推进
     */
    void advanceStage(Long oppId, Long currentUserId);
}
