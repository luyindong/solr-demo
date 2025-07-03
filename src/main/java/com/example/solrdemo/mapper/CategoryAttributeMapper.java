package com.example.solrdemo.mapper;

import com.example.solrdemo.entity.CategoryAttribute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryAttributeMapper {
    
    // 根据类目ID查询属性列表
    List<CategoryAttribute> selectByCategoryId(@Param("categoryId") Long categoryId);
    
    // 根据类目ID列表查询属性列表
    List<CategoryAttribute> selectByCategoryIds(@Param("categoryIds") List<Long> categoryIds);
    
    // 插入属性
    int insert(CategoryAttribute attribute);
    
    // 更新属性
    int update(CategoryAttribute attribute);
    
    // 删除属性
    int deleteById(@Param("id") Long id);
    
    // 根据类目ID删除属性
    int deleteByCategoryId(@Param("categoryId") Long categoryId);
    
    // 根据ID查询属性
    CategoryAttribute selectById(@Param("id") Long id);
    
    // 根据类目ID和属性编码查询属性
    CategoryAttribute selectByCategoryIdAndCode(@Param("categoryId") Long categoryId, @Param("attributeCode") String attributeCode);
} 