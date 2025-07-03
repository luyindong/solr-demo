package com.example.solrdemo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CategoryAttribute {
    private Long id;
    private Long categoryId;      // 关联的类目ID
    private String attributeName; // 属性名称，如：颜色、尺寸、材质等
    private String attributeCode; // 属性编码，如：color、size、material等
    private String attributeType; // 属性类型：text(文本)、number(数字)、select(选择)、range(范围)
    private String options;       // 选项值，JSON格式，用于select类型
    private String unit;          // 单位，如：cm、kg等
    private Integer sort;         // 排序
    private Integer required;     // 是否必填 0-否 1-是
    private Integer status;       // 0-禁用 1-启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 