package com.example.solrdemo.dto;

import com.example.solrdemo.entity.Product;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ProductSearchResult {
    private List<Product> products;           // 商品列表
    private List<CategoryOption> categories;  // 可选的类目
    private List<BrandOption> brands;         // 可选的品牌
    private Map<String, List<AttributeOption>> attributeFacets; // 属性分面结果
    private PriceRange priceRange;            // 价格区间
    private Long total;                       // 总记录数
    private Integer page;                     // 当前页码
    private Integer size;                     // 每页大小
    private Integer totalPages;               // 总页数

    @Data
    public static class BrandOption {
        private Long id;
        private String name;
        private Long count;  // 该品牌下的商品数量
    }

    @Data
    public static class CategoryOption {
        private Long id;
        private String name;
        private Long count;  // 该类目下的商品数量
    }

    @Data
    public static class PriceRange {
        private Double minPrice;
        private Double maxPrice;
    }

    @Data
    public static class AttributeOption {
        private String code;      // 属性编码
        private String name;      // 属性名称
        private String value;     // 属性值
        private Long count;       // 该属性值下的商品数量
    }
}