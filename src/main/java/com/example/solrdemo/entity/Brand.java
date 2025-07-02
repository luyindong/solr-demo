package com.example.solrdemo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Brand {
    private Long id;
    private String name;
    private String logo;
    private String description;
    private Integer sort;         // 排序
    private Integer status;       // 0-禁用 1-启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 