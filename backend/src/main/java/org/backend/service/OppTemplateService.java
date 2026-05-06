package org.backend.service;

import org.backend.model.Dto.opp.OppTemplateDto;

import java.util.List;

public interface OppTemplateService {
    /** 全部商机模板(含每个模板的环节定义) */
    List<OppTemplateDto> listAll();
}
