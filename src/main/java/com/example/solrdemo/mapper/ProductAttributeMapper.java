package com.example.solrdemo.mapper;

import com.example.solrdemo.entity.ProductAttribute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductAttributeMapper {
    
    // 根据商品ID查询属性列表
    List<ProductAttribute> selectByProductId(@Param("productId") Long productId);
    
    // 根据商品ID列表查询属性列表
    List<ProductAttribute> selectByProductIds(@Param("productIds") List<Long> productIds);
    
    // 根据属性编码和值查询商品ID列表
    List<Long> selectProductIdsByAttribute(@Param("attributeCode") String attributeCode, 
                                         @Param("attributeValue") String attributeValue);
    
    // 根据属性编码和值范围查询商品ID列表
    List<Long> selectProductIdsByAttributeRange(@Param("attributeCode") String attributeCode,
                                               @Param("minValue") String minValue,
                                               @Param("maxValue") String maxValue);
    
    // 根据属性编码和值列表查询商品ID列表
    List<Long> selectProductIdsByAttributeValues(@Param("attributeCode") String attributeCode,
                                                @Param("attributeValues") List<String> attributeValues);
    
    // 批量插入属性
    int batchInsert(@Param("attributes") List<ProductAttribute> attributes);
    
    // 更新属性
    int update(ProductAttribute attribute);
    
    // 根据商品ID删除属性
    int deleteByProductId(@Param("productId") Long productId);
    
    // 根据ID删除属性
    int deleteById(@Param("id") Long id);
} 