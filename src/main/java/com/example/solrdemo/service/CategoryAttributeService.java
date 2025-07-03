package com.example.solrdemo.service;

import com.example.solrdemo.entity.CategoryAttribute;
import com.example.solrdemo.mapper.CategoryAttributeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryAttributeService {
    
    @Autowired
    private CategoryAttributeMapper categoryAttributeMapper;
    
    /**
     * 根据类目ID查询属性列表
     */
    public List<CategoryAttribute> getAttributesByCategoryId(Long categoryId) {
        return categoryAttributeMapper.selectByCategoryId(categoryId);
    }
    
    /**
     * 根据类目ID列表查询属性列表
     */
    public List<CategoryAttribute> getAttributesByCategoryIds(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new ArrayList<>();
        }
        return categoryAttributeMapper.selectByCategoryIds(categoryIds);
    }
    
    /**
     * 添加类目属性
     */
    @Transactional
    public void addAttribute(CategoryAttribute attribute) {
        if (attribute.getSort() == null) {
            attribute.setSort(0);
        }
        if (attribute.getRequired() == null) {
            attribute.setRequired(0);
        }
        if (attribute.getStatus() == null) {
            attribute.setStatus(1);
        }
        categoryAttributeMapper.insert(attribute);
    }
    
    /**
     * 更新类目属性
     */
    @Transactional
    public void updateAttribute(CategoryAttribute attribute) {
        categoryAttributeMapper.update(attribute);
    }
    
    /**
     * 删除类目属性
     */
    @Transactional
    public void deleteAttribute(Long id) {
        categoryAttributeMapper.deleteById(id);
    }
    
    /**
     * 根据类目ID删除所有属性
     */
    @Transactional
    public void deleteAttributesByCategoryId(Long categoryId) {
        categoryAttributeMapper.deleteByCategoryId(categoryId);
    }
    
    /**
     * 根据ID查询属性
     */
    public CategoryAttribute getAttributeById(Long id) {
        return categoryAttributeMapper.selectById(id);
    }
    
    /**
     * 根据类目ID和属性编码查询属性
     */
    public CategoryAttribute getAttributeByCode(Long categoryId, String attributeCode) {
        return categoryAttributeMapper.selectByCategoryIdAndCode(categoryId, attributeCode);
    }
} 