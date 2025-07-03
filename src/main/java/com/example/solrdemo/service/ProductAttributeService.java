package com.example.solrdemo.service;

import com.example.solrdemo.entity.ProductAttribute;
import com.example.solrdemo.mapper.ProductAttributeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductAttributeService {
    
    @Autowired
    private ProductAttributeMapper productAttributeMapper;
    
    /**
     * 根据商品ID查询属性列表
     */
    public List<ProductAttribute> getAttributesByProductId(Long productId) {
        return productAttributeMapper.selectByProductId(productId);
    }
    
    /**
     * 根据商品ID列表查询属性列表
     */
    public List<ProductAttribute> getAttributesByProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return new ArrayList<>();
        }
        return productAttributeMapper.selectByProductIds(productIds);
    }
    
    /**
     * 根据属性筛选条件查询商品ID列表
     */
    public List<Long> getProductIdsByAttributeFilter(String attributeCode, Object attributeValue) {
        if (attributeValue instanceof List) {
            // 多选值
            @SuppressWarnings("unchecked")
            List<String> values = (List<String>) attributeValue;
            return productAttributeMapper.selectProductIdsByAttributeValues(attributeCode, values);
        } else if (attributeValue instanceof java.util.Map) {
            // 范围值
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> range = (java.util.Map<String, Object>) attributeValue;
            String minValue = (String) range.get("min");
            String maxValue = (String) range.get("max");
            return productAttributeMapper.selectProductIdsByAttributeRange(attributeCode, minValue, maxValue);
        } else {
            // 单选值
            String value = attributeValue != null ? attributeValue.toString() : null;
            return productAttributeMapper.selectProductIdsByAttribute(attributeCode, value);
        }
    }
    
    /**
     * 批量保存商品属性
     */
    @Transactional
    public void batchSaveAttributes(List<ProductAttribute> attributes) {
        if (attributes != null && !attributes.isEmpty()) {
            productAttributeMapper.batchInsert(attributes);
        }
    }
    
    /**
     * 更新商品属性
     */
    @Transactional
    public void updateAttribute(ProductAttribute attribute) {
        productAttributeMapper.update(attribute);
    }
    
    /**
     * 根据商品ID删除所有属性
     */
    @Transactional
    public void deleteAttributesByProductId(Long productId) {
        productAttributeMapper.deleteByProductId(productId);
    }
    
    /**
     * 根据ID删除属性
     */
    @Transactional
    public void deleteAttribute(Long id) {
        productAttributeMapper.deleteById(id);
    }
} 