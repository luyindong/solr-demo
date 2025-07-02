package com.example.solrdemo.dto;

import lombok.Data;

@Data
public class PageRequest {
    private Integer page = 1;         // 页码
    private Integer size = 20;        // 每页大小

    public Integer getOffset() {
        int page = getPage() != null ? getPage() : 1;
        int size = getSize() != null ? getSize() : 20;
        page = Math.max(1, page);
        size = Math.max(1, Math.min(1000, size));
        int offset = (page - 1) * size;
        return Math.max(0, offset);
    }

    public Integer getValidPage() {
        return getPage() != null ? Math.max(1, getPage()) : 1;
    }

    public Integer getValidSize() {
        return getSize() != null ? Math.max(1, Math.min(1000, getSize())) : 20;
    }
}
