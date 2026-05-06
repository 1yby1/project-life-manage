package org.backend.controller;

import org.backend.model.Dto.opp.OppTemplateDto;
import org.backend.service.OppTemplateService;
import org.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/opp-templates")
public class OppTemplateController {

    @Autowired
    private OppTemplateService templateService;

    @GetMapping
    public Result<List<OppTemplateDto>> list() {
        return Result.success(templateService.listAll());
    }
}
