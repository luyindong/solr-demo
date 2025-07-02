package com.example.solrdemo.dto;

import com.example.solrdemo.entity.Product;
import lombok.Data;

import java.util.List;

@Data
public class ProductSearchResult {
    private List<Product> products;           // 商品列表
    private List<CategoryOption> categories;  // 可选的类目
    private List<BrandOption> brands;         // 可选的品牌
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
}