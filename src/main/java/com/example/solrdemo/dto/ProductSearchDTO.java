package com.example.solrdemo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ProductSearchDTO extends PageRequest {
    private String keyword;           // 关键词搜索
    private List<Long> categoryIds;   // 类目ID列表
    private List<Long> brandIds;      // 品牌ID列表
    private BigDecimal minPrice;      // 最低价格
    private BigDecimal maxPrice;      // 最高价格
    private String sortField;         // 排序字段
    private String sortOrder;         // 排序方向 asc/desc

    // 动态属性筛选
    private Map<String, Object> attributes; // 属性筛选，key为属性编码，value为属性值或值范围

    // 获取属性值（支持单个值）
    public String getAttributeValue(String attributeCode) {
        if (attributes != null && attributes.containsKey(attributeCode)) {
            Object value = attributes.get(attributeCode);
            return value != null ? value.toString() : null;
        }
        return null;
    }

    // 获取属性值列表（支持多选）
    @SuppressWarnings("unchecked")
    public List<String> getAttributeValues(String attributeCode) {
        if (attributes != null && attributes.containsKey(attributeCode)) {
            Object value = attributes.get(attributeCode);
            if (value instanceof List) {
                return (List<String>) value;
            } else if (value != null) {
                List<String> result = new ArrayList<>();
                result.add(value.toString());
                return result;
            }
        }
        return null;
    }

    // 获取属性值范围（支持范围查询）
    @SuppressWarnings("unchecked")
    public Map<String, Object> getAttributeRange(String attributeCode) {
        if (attributes != null && attributes.containsKey(attributeCode)) {
            Object value = attributes.get(attributeCode);
            if (value instanceof Map) {
                return (Map<String, Object>) value;
            }
        }
        return null;
    }
}