package com.example.solrdemo.controller;

import com.example.solrdemo.entity.Category;
import com.example.solrdemo.mapper.CategoryMapper;
import com.example.solrdemo.mapper.ProductMapper;
import com.example.solrdemo.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/product/category")
public class CategoryController {
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("")
    public String list(Model model) {
        List<Category> categories = categoryMapper.selectAll();
        List<Category> level1 = new java.util.ArrayList<>();
        java.util.Map<Long, Category> idMap = new java.util.HashMap<>();
        for (Category cat : categories) {
            idMap.put(cat.getId(), cat);
            if (cat.getLevel() == 1) {
                level1.add(cat);
            }
        }
        for (Category cat : categories) {
            if (cat.getLevel() == 2 && cat.getParentId() != null) {
                Category parent = idMap.get(cat.getParentId());
                if (parent != null) {
                    parent.getChildren().add(cat);
                }
            }
        }
        log.info("level1 {}", level1);
        model.addAttribute("level1", level1);
        model.addAttribute("categories", categories);
        model.addAttribute("category", new Category());
        model.addAttribute("editMode", false);
        return "product/category";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        List<Category> categories = categoryMapper.selectAll();
        Category category = categoryMapper.selectById(id);
        model.addAttribute("categories", categories);
        model.addAttribute("category", category);
        model.addAttribute("editMode", true);
        return "product/category";
    }

    @GetMapping("/detail")
    @ResponseBody
    public Category detail(@RequestParam Long id) {
        return categoryMapper.selectById(id);
    }

    @PostMapping(value = "/add", consumes = {"application/json", "application/x-www-form-urlencoded"})
    @ResponseBody
    public String addCategory(@RequestBody(required = false) Category category, @ModelAttribute Category formCategory) {
        try {
            Category cat = category != null ? category : formCategory;
            categoryService.addCategory(cat);
            return "success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @PostMapping(value = "/update", consumes = {"application/json", "application/x-www-form-urlencoded"})
    @ResponseBody
    public String updateCategory(@RequestBody(required = false) Category category, @ModelAttribute Category formCategory) {
        try {
            Category cat = category != null ? category : formCategory;
            categoryService.updateCategoryAndSync(cat);
            return "success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("msg", "类目删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/product/category";
    }
} 