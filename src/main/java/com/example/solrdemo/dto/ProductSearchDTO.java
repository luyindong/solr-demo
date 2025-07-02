package com.example.solrdemo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductSearchDTO extends PageRequest {
    private String keyword;           // 关键词搜索
    private List<Long> categoryIds;   // 类目ID列表
    private List<Long> brandIds;      // 品牌ID列表
    private BigDecimal minPrice;      // 最低价格
    private BigDecimal maxPrice;      // 最高价格
    private String sortField;         // 排序字段
    private String sortOrder;         // 排序方向 asc/desc
}