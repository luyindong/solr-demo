package com.example.solrdemo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductAttribute {
    private Long id;
    private Long productId;       // 关联的商品ID
    private Long attributeId;     // 关联的属性ID
    private String attributeCode; // 属性编码
    private String attributeName; // 属性名称
    private String attributeValue; // 属性值
    private String attributeType; // 属性类型
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 