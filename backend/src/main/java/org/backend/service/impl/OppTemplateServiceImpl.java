package org.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.backend.mapper.OppTemplateMapper;
import org.backend.mapper.OppTemplateStageMapper;
import org.backend.model.CrmOppTemplate;
import org.backend.model.CrmOppTemplateStage;
import org.backend.model.Dto.opp.OppTemplateDto;
import org.backend.model.Dto.opp.TemplateStageDto;
import org.backend.service.OppTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OppTemplateServiceImpl implements OppTemplateService {

    @Autowired
    private OppTemplateMapper templateMapper;
    @Autowired
    private OppTemplateStageMapper templateStageMapper;

    @Override
    public List<OppTemplateDto> listAll() {
        List<CrmOppTemplate> templates = templateMapper.selectList(null);
        List<CrmOppTemplateStage> allStages = templateStageMapper.selectList(
                new LambdaQueryWrapper<CrmOppTemplateStage>().orderByAsc(CrmOppTemplateStage::getSortOrder)
        );
        Map<Long, List<CrmOppTemplateStage>> grouped = allStages.stream()
                .collect(Collectors.groupingBy(CrmOppTemplateStage::getTemplateId));

        return templates.stream().map(t -> {
            OppTemplateDto dto = new OppTemplateDto();
            dto.setId(t.getId());
            dto.setTemplateName(t.getTemplateName());
            dto.setDescription(t.getDescription());
            dto.setIsDefault(t.getIsDefault() != null && t.getIsDefault() == 1);
            List<TemplateStageDto> sts = grouped.getOrDefault(t.getId(), Collections.emptyList()).stream()
                    .map(s -> {
                        TemplateStageDto sd = new TemplateStageDto();
                        sd.setId(s.getId());
                        sd.setStageCode(s.getStageCode());
                        sd.setStageName(s.getStageName());
                        sd.setSortOrder(s.getSortOrder());
                        sd.setRequired(s.getRequired() != null && s.getRequired() == 1);
                        return sd;
                    }).collect(Collectors.toList());
            dto.setStages(sts);
            return dto;
        }).collect(Collectors.toList());
    }
}
