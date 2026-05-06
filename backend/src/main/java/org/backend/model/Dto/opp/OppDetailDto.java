package org.backend.model.Dto.opp;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 商机详情 = 列表 row + 环节实例 + 任务列表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OppDetailDto extends OppListItemDto {
    private List<OppStageDto> stages;
    private List<TaskListItemDto> tasks;
}
