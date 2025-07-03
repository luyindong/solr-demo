package com.example.solrdemo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Category {
    private Long id;
    private String name;
    private Long parentId;
    private Integer level;        // 类目层级 1-一级类目 2-二级类目
    private Integer sort;         // 排序
    private Integer status;       // 0-禁用 1-启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    // 嵌套子类目
    private java.util.List<Category> children = new java.util.ArrayList<>();
} 