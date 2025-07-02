package com.example.solrdemo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;  // 冗余字段
    private Long brandId;
    private String brandName;     // 冗余字段
    private BigDecimal price;
    private String description;
    private String image;
    private Integer stock;
    private Integer status;       // 0-下架 1-上架
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 