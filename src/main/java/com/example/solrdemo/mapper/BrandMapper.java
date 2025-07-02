package com.example.solrdemo.mapper;

import com.example.solrdemo.entity.Brand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BrandMapper {
    
    List<Brand> selectAll();
    
    Brand selectById(@Param("id") Long id);
    
    List<Brand> selectByStatus(@Param("status") Integer status);
    
    int insert(Brand brand);
    
    int update(Brand brand);
    
    int deleteById(@Param("id") Long id);
} 