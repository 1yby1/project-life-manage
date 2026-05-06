package org.backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.backend.mapper.CustomerMapper;
import org.backend.mapper.LeadMapper;
import org.backend.model.CrmCustomer;
import org.backend.model.CrmLead;
import org.backend.model.CrmOpportunity;
import org.backend.model.Dto.lead.LeadCollectRequest;
import org.backend.model.Dto.lead.LeadConvertResponse;
import org.backend.model.Dto.lead.LeadCreateRequest;
import org.backend.model.Dto.lead.LeadCultivateRequest;
import org.backend.model.Dto.lead.LeadDetailDto;
import org.backend.model.Dto.lead.LeadDistributeRequest;
import org.backend.model.Dto.lead.LeadListItemDto;
import org.backend.service.LeadService;
import org.backend.service.OpportunityService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeadServiceImpl implements LeadService {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    private LeadMapper leadMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private OpportunityService opportunityService;

    @Override
    public Long create(LeadCreateRequest req, Long currentUserId) {
        if (req.getCustomerId() == null) throw new IllegalArgumentException("客户必填");
        if (isBlank(req.getTitle())) throw new IllegalArgumentException("线索名称必填");
        if (isBlank(req.getRequirement())) throw new IllegalArgumentException("线索描述必填");
        if (currentUserId == null) throw new IllegalArgumentException("未登录");

        CrmCustomer customer = customerMapper.selectById(req.getCustomerId());
        if (customer == null) throw new IllegalArgumentException("客户不存在: " + req.getCustomerId());

        CrmLead lead = new CrmLead();
        lead.setCustomerId(req.getCustomerId());
        lead.setTitle(req.getTitle());
        lead.setRequirement(req.getRequirement());
        lead.setBu(req.getBu());
        lead.setStatus("ENTRY");
        lead.setEntryBy(currentUserId);
        lead.setCreateTime(LocalDateTime.now());
        lead.setUpdateTime(LocalDateTime.now());
        leadMapper.insert(lead);
        return lead.getId();
    }

    @Override
    public List<LeadListItemDto> list(String filter,
                                      String keyword,
                                      String bu,
                                      String status,
                                      Long currentUserId,
                                      List<String> currentUserRoles) {
        // 默认隐藏 CONVERTED;按 filter 计算 entryBy/managerId/participantUserId
        boolean excludeConverted = !"converted".equalsIgnoreCase(status);

        Long entryBy = null;
        Long managerId = null;
        Long participantUserId = null;
        String effectiveStatus = blankToNull(status);

        if (currentUserId != null) {
            switch (filter == null ? "all" : filter) {
                case "mine":
                    entryBy = currentUserId;
                    break;
                case "participate":
                    participantUserId = currentUserId;
                    break;
                case "todo":
                    if (hasRole(currentUserRoles, "OPP_ADMIN")) {
                        effectiveStatus = "COLLECTED";
                    } else if (hasRole(currentUserRoles, "CUSTOMER_MANAGER")) {
                        managerId = currentUserId;
                        effectiveStatus = "DISTRIBUTED";
                    } else {
                        // SALES/USER: 我自己录入且待收集的
                        entryBy = currentUserId;
                        effectiveStatus = "ENTRY";
                    }
                    break;
                case "all":
                default:
                    break;
            }
        }

        return leadMapper.selectListWithJoins(
                blankToNull(keyword),
                blankToNull(bu),
                effectiveStatus,
                excludeConverted,
                entryBy,
                managerId,
                participantUserId);
    }

    @Override
    public LeadDetailDto detail(Long leadId) {
        if (leadId == null) return null;
        LeadListItemDto base = leadMapper.findByIdWithJoins(leadId);
        if (base == null) return null;
        LeadDetailDto dto = new LeadDetailDto();
        BeanUtils.copyProperties(base, dto);
        // 反序列化 progress_desc JSON
        if (base.getProgressDesc() != null && !base.getProgressDesc().isBlank()) {
            try {
                LeadCultivateRequest info = JSON.readValue(base.getProgressDesc(), LeadCultivateRequest.class);
                // winRate 用列上的(优先)
                if (base.getWinRate() != null) info.setWinRate(base.getWinRate());
                dto.setCultivateInfo(info);
            } catch (JsonProcessingException ignored) {
                // JSON 损坏时不阻塞,前端可手工修
            }
        }
        return dto;
    }

    @Override
    public void saveDraft(Long leadId, LeadCollectRequest req, Long currentUserId) {
        CrmLead lead = mustLoad(leadId);
        ensureEntryOwner(lead, currentUserId);
        if (!"ENTRY".equals(lead.getStatus())) {
            throw new IllegalArgumentException("当前阶段不可临时保存(状态: " + lead.getStatus() + ")");
        }
        applyBaseFields(lead, req);
        lead.setUpdateTime(LocalDateTime.now());
        leadMapper.updateById(lead);
    }

    @Override
    public void collect(Long leadId, LeadCollectRequest req, Long currentUserId) {
        CrmLead lead = mustLoad(leadId);
        ensureEntryOwner(lead, currentUserId);
        if (!"ENTRY".equals(lead.getStatus())) {
            throw new IllegalArgumentException("仅 ENTRY 状态可确认收集(状态: " + lead.getStatus() + ")");
        }
        applyBaseFields(lead, req);
        // 录入人即收集人
        lead.setCollectorBy(currentUserId);
        lead.setStatus("COLLECTED");
        lead.setUpdateTime(LocalDateTime.now());
        leadMapper.updateById(lead);
    }

    @Override
    public void distribute(Long leadId, LeadDistributeRequest req, Long currentUserId) {
        if (req.getManagerId() == null) throw new IllegalArgumentException("客户经理 ID 必填");
        CrmLead lead = mustLoad(leadId);
        if (!"COLLECTED".equals(lead.getStatus())) {
            throw new IllegalArgumentException("仅 COLLECTED 状态可分发(状态: " + lead.getStatus() + ")");
        }
        lead.setStatus("DISTRIBUTED");
        lead.setDistributorBy(currentUserId);
        lead.setManagerId(req.getManagerId());
        lead.setUpdateTime(LocalDateTime.now());
        leadMapper.updateById(lead);
    }

    @Override
    public void cultivate(Long leadId, LeadCultivateRequest req, Long currentUserId) {
        CrmLead lead = mustLoad(leadId);
        ensureManagerOwner(lead, currentUserId);
        if (!"DISTRIBUTED".equals(lead.getStatus())) {
            throw new IllegalArgumentException("仅 DISTRIBUTED 状态可培育(状态: " + lead.getStatus() + ")");
        }
        // win_rate 列单独存
        if (req.getWinRate() != null) {
            lead.setWinRate(req.getWinRate());
        }
        // 其余 7 字段(含 keyContact)序列化为 JSON 存 progress_desc
        try {
            // 不把 winRate 一起写进 JSON(避免重复;但反序列化时会再回填)
            BigDecimal originalWinRate = req.getWinRate();
            req.setWinRate(null);
            String json = JSON.writeValueAsString(req);
            req.setWinRate(originalWinRate);
            lead.setProgressDesc(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("培育详情序列化失败: " + e.getMessage());
        }
        lead.setUpdateTime(LocalDateTime.now());
        leadMapper.updateById(lead);
    }

    @Override
    public LeadConvertResponse convert(Long leadId, Long currentUserId) {
        CrmLead lead = mustLoad(leadId);
        ensureManagerOwner(lead, currentUserId);
        if (!"DISTRIBUTED".equals(lead.getStatus())) {
            throw new IllegalArgumentException("仅 DISTRIBUTED 状态可转商机(状态: " + lead.getStatus() + ")");
        }
        // 必须先培育(progress_desc 非空 + opportunityOwnerId 已填)
        LeadCultivateRequest info;
        if (lead.getProgressDesc() == null || lead.getProgressDesc().isBlank()) {
            throw new IllegalArgumentException("请先完善培育详情再转商机");
        }
        try {
            info = JSON.readValue(lead.getProgressDesc(), LeadCultivateRequest.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("培育详情解析失败,请重新填写");
        }
        if (info.getOpportunityOwnerId() == null) {
            throw new IllegalArgumentException("请先指定商机负责人(项目经理)");
        }

        CrmCustomer customer = customerMapper.selectById(lead.getCustomerId());
        String customerName = customer == null ? "客户" : customer.getCustomerName();
        String oppName = customerName + "-" + lead.getTitle() + "-商机";

        CrmOpportunity opp = opportunityService.createPlaceholderFromLead(
                lead.getId(),
                lead.getCustomerId(),
                oppName,
                info.getOpportunityOwnerId(),
                info);

        lead.setStatus("CONVERTED");
        lead.setUpdateTime(LocalDateTime.now());
        leadMapper.updateById(lead);

        LeadConvertResponse resp = new LeadConvertResponse();
        resp.setOpportunityId(opp.getId());
        resp.setOpportunityName(opp.getOppName());
        return resp;
    }

    // ========== 私有工具 ==========

    private CrmLead mustLoad(Long leadId) {
        if (leadId == null) throw new IllegalArgumentException("线索 ID 必填");
        CrmLead lead = leadMapper.selectById(leadId);
        if (lead == null) throw new IllegalArgumentException("线索不存在: " + leadId);
        return lead;
    }

    private void ensureEntryOwner(CrmLead lead, Long currentUserId) {
        if (currentUserId == null || !currentUserId.equals(lead.getEntryBy())) {
            throw new IllegalArgumentException("仅录入人可操作此阶段");
        }
    }

    private void ensureManagerOwner(CrmLead lead, Long currentUserId) {
        if (currentUserId == null || !currentUserId.equals(lead.getManagerId())) {
            throw new IllegalArgumentException("仅被指派的客户经理可操作此阶段");
        }
    }

    private void applyBaseFields(CrmLead lead, LeadCollectRequest req) {
        if (req == null) return;
        if (req.getCustomerId() != null) lead.setCustomerId(req.getCustomerId());
        if (!isBlank(req.getTitle())) lead.setTitle(req.getTitle());
        if (!isBlank(req.getRequirement())) lead.setRequirement(req.getRequirement());
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private static boolean hasRole(List<String> roles, String code) {
        return roles != null && roles.contains(code);
    }
}
