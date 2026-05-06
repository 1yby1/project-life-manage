package org.backend.model.Dto;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private long total;
    private List<T> records;

    public PageResult(long total, List<T> records) {
        this.total = total;
        this.records = records;
    }
}
