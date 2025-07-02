package com.example.solrdemo.controller;

import com.example.solrdemo.entity.Category;
import com.example.solrdemo.mapper.CategoryMapper;
import com.example.solrdemo.mapper.ProductMapper;
import com.example.solrdemo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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

    @PostMapping("/add")
    public String add(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        try {
            categoryService.addCategory(category);
            redirectAttributes.addFlashAttribute("msg", "类目添加成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/product/category";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        categoryService.updateCategoryAndSync(category);
        redirectAttributes.addFlashAttribute("msg", "类目修改成功！");
        return "redirect:/product/category";
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