package com.example.solrdemo.controller;

import com.example.solrdemo.entity.CategoryAttribute;
import com.example.solrdemo.service.CategoryAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/product/attribute")
public class CategoryAttributeController {
    
    @Autowired
    private CategoryAttributeService categoryAttributeService;
    
    /**
     * 类目属性管理页面
     */
    @GetMapping("/list")
    public String list(@RequestParam Long categoryId, Model model) {
        List<CategoryAttribute> attributes = categoryAttributeService.getAttributesByCategoryId(categoryId);
        model.addAttribute("attributes", attributes);
        model.addAttribute("categoryId", categoryId);
        return "product/attribute";
    }
    
    /**
     * 添加属性
     */
    @PostMapping("/add")
    @ResponseBody
    public String add(@RequestBody CategoryAttribute attribute) {
        try {
            categoryAttributeService.addAttribute(attribute);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    /**
     * 更新属性
     */
    @PostMapping("/update")
    @ResponseBody
    public String update(@RequestBody CategoryAttribute attribute) {
        try {
            categoryAttributeService.updateAttribute(attribute);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    /**
     * 删除属性
     */
    @PostMapping("/delete")
    @ResponseBody
    public String delete(@RequestParam Long id) {
        try {
            categoryAttributeService.deleteAttribute(id);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    /**
     * 获取属性详情
     */
    @GetMapping("/detail")
    @ResponseBody
    public CategoryAttribute detail(@RequestParam Long id) {
        return categoryAttributeService.getAttributeById(id);
    }
    
    /**
     * 根据类目ID列表获取属性（支持多个类目，合并去重）
     */
    @GetMapping("/filters")
    @ResponseBody
    public List<CategoryAttribute> getAttributesByCategoryIds(@RequestParam String categoryIds) {
        List<CategoryAttribute> result = new ArrayList<>();
        if (categoryIds == null || categoryIds.trim().isEmpty()) {
            return result;
        }
        String[] idArr = categoryIds.split(",");
        List<Long> idList = new ArrayList<>();
        for (String idStr : idArr) {
            try {
                idList.add(Long.valueOf(idStr.trim()));
            } catch (Exception ignored) {
            }
        }
        // 合并所有类目的属性，去重（按attributeCode）
        java.util.Map<String, CategoryAttribute> attrMap = new java.util.HashMap<>();
        for (Long categoryId : idList) {
            List<CategoryAttribute> attrs = categoryAttributeService.getAttributesByCategoryId(categoryId);
            for (CategoryAttribute attr : attrs) {
                attrMap.put(attr.getAttributeCode(), attr);
            }
        }
        result.addAll(attrMap.values());
        return result;
    }
} 