package org.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.backend.mapper.OppStageMapper;
import org.backend.mapper.OppTaskMapper;
import org.backend.mapper.OppTemplateStageMapper;
import org.backend.mapper.OpportunityMapper;
import org.backend.model.CrmOppStage;
import org.backend.model.CrmOppTemplateStage;
import org.backend.model.CrmOpportunity;
import org.backend.model.Dto.lead.LeadCultivateRequest;
import org.backend.model.Dto.opp.OppDetailDto;
import org.backend.model.Dto.opp.OppListItemDto;
import org.backend.service.OpportunityService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class OpportunityServiceImpl implements OpportunityService {

    @Autowired
    private OpportunityMapper opportunityMapper;
    @Autowired
    private OppStageMapper stageMapper;
    @Autowired
    private OppTaskMapper taskMapper;
    @Autowired
    private OppTemplateStageMapper templateStageMapper;

    @Override
    public CrmOpportunity createPlaceholderFromLead(Long leadId,
                                                    Long customerId,
                                                    String opportunityName,
                                                    Long ownerId,
                                                    LeadCultivateRequest cultivateInfo) {
        CrmOpportunity opp = new CrmOpportunity();
        opp.setLeadId(leadId);
        opp.setCustomerId(customerId);
        opp.setOppName(opportunityName);
        opp.setStage("VALIDATE");
        opp.setManagerId(ownerId);
        opp.setPmId(ownerId);
        // sm_id / dm_id / template_id 留空,等商机模块完整实现时再填
        opp.setCreateTime(LocalDateTime.now());
        opp.setUpdateTime(LocalDateTime.now());
        opportunityMapper.insert(opp);
        return opp;
    }

    @Override
    public List<OppListItemDto> list(String keyword, Long customerId, String stage) {
        return opportunityMapper.selectListWithJoins(null, keyword, customerId, stage);
    }

    @Override
    public OppDetailDto detail(Long oppId) {
        if (oppId == null) return null;
        List<OppListItemDto> rows = opportunityMapper.selectListWithJoins(oppId, null, null, null);
        if (rows.isEmpty()) return null;
        OppListItemDto opp = rows.get(0);

        OppDetailDto dto = new OppDetailDto();
        BeanUtils.copyProperties(opp, dto);
        dto.setStages(stageMapper.selectByOppIdWithOwnerName(oppId));
        dto.setTasks(taskMapper.selectListWithJoins(oppId, null));
        return dto;
    }

    @Override
    public void applyTemplate(Long oppId, Long templateId, Long currentUserId) {
        if (oppId == null) throw new IllegalArgumentException("商机 ID 必填");
        if (templateId == null) throw new IllegalArgumentException("模板 ID 必填");

        CrmOpportunity opp = opportunityMapper.selectById(oppId);
        if (opp == null) throw new IllegalArgumentException("商机不存在: " + oppId);
        if (!Objects.equals(opp.getPmId(), currentUserId)) {
            throw new IllegalArgumentException("仅商机的项目经理可应用模板");
        }

        // 取模板的环节定义
        LambdaQueryWrapper<CrmOppTemplateStage> tsq = new LambdaQueryWrapper<>();
        tsq.eq(CrmOppTemplateStage::getTemplateId, templateId)
                .orderByAsc(CrmOppTemplateStage::getSortOrder);
        List<CrmOppTemplateStage> tplStages = templateStageMapper.selectList(tsq);
        if (tplStages.isEmpty()) {
            throw new IllegalArgumentException("模板未定义环节: " + templateId);
        }

        // 删除该 opp 已有 stages(允许重新选模板,但只在没生成任务前重新选)
        Long taskCount = taskMapper.selectCount(
                new LambdaQueryWrapper<org.backend.model.CrmOppTask>().eq(org.backend.model.CrmOppTask::getOppId, oppId)
        );
        if (taskCount != null && taskCount > 0) {
            throw new IllegalArgumentException("该商机已生成任务,不可重新选模板");
        }
        stageMapper.delete(new LambdaQueryWrapper<CrmOppStage>().eq(CrmOppStage::getOppId, oppId));

        // 按模板生成 opp_stage 实例(owner_id 留空,默认由铁三角操作)
        for (CrmOppTemplateStage ts : tplStages) {
            CrmOppStage s = new CrmOppStage();
            s.setOppId(oppId);
            s.setStageCode(ts.getStageCode());
            s.setStageName(ts.getStageName());
            s.setSortOrder(ts.getSortOrder());
            s.setStatus("PENDING");
            stageMapper.insert(s);
        }

        // 写回 opp.template_id
        opp.setTemplateId(templateId);
        opp.setUpdateTime(LocalDateTime.now());
        opportunityMapper.updateById(opp);
    }

    @Override
    public void setStageOwner(Long stageId, Long ownerId, Long currentUserId) {
        if (stageId == null) throw new IllegalArgumentException("环节 ID 必填");

        CrmOppStage stage = stageMapper.selectById(stageId);
        if (stage == null) throw new IllegalArgumentException("环节不存在: " + stageId);

        CrmOpportunity opp = opportunityMapper.selectById(stage.getOppId());
        if (opp == null) throw new IllegalArgumentException("商机不存在");

        boolean isPM = Objects.equals(opp.getPmId(), currentUserId);
        boolean isCurrentOwner = Objects.equals(stage.getOwnerId(), currentUserId);
        if (!isPM && !isCurrentOwner) {
            throw new IllegalArgumentException("无权修改此环节责任人");
        }

        stage.setOwnerId(ownerId);
        stageMapper.updateById(stage);
    }

    /** 阶段顺序: 索引值即顺序; -1 表示未知 */
    private static final List<String> STAGE_ORDER = java.util.Arrays.asList("VALIDATE", "NEGOTIATE", "IMPLEMENT", "DELIVERY");

    @Override
    public void advanceStage(Long oppId, Long currentUserId) {
        if (oppId == null) throw new IllegalArgumentException("商机 ID 必填");

        CrmOpportunity opp = opportunityMapper.selectById(oppId);
        if (opp == null) throw new IllegalArgumentException("商机不存在: " + oppId);

        if (!Objects.equals(opp.getPmId(), currentUserId)) {
            throw new IllegalArgumentException("仅商机的项目经理可推进阶段");
        }

        int idx = STAGE_ORDER.indexOf(opp.getStage());
        if (idx < 0) throw new IllegalArgumentException("未知商机阶段: " + opp.getStage());
        if (idx >= STAGE_ORDER.size() - 1) {
            throw new IllegalArgumentException("商机已处于最终阶段(DELIVERY),不可再推进");
        }

        String next = STAGE_ORDER.get(idx + 1);
        opp.setStage(next);
        opp.setUpdateTime(LocalDateTime.now());
        opportunityMapper.updateById(opp);
    }
}
