package com.example.solrdemo.mapper;

import com.example.solrdemo.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CategoryMapper {
    
    List<Category> selectAll();
    
    Category selectById(@Param("id") Long id);
    
    List<Category> selectByParentId(@Param("parentId") Long parentId);
    
    List<Category> selectByLevel(@Param("level") Integer level);
    
    int insert(Category category);
    
    int update(Category category);
    
    int deleteById(@Param("id") Long id);
} 