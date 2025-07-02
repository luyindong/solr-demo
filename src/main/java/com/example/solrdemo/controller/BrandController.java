package com.example.solrdemo.controller;

import com.example.solrdemo.entity.Brand;
import com.example.solrdemo.mapper.BrandMapper;
import com.example.solrdemo.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/product/brand")
public class BrandController {
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private BrandService brandService;


    @GetMapping("")
    public String list(Model model) {
        List<Brand> brands = brandMapper.selectAll();
        model.addAttribute("brands", brands);
        model.addAttribute("brand", new Brand());
        model.addAttribute("editMode", false);
        return "product/brand";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        List<Brand> brands = brandMapper.selectAll();
        Brand brand = brandMapper.selectById(id);
        model.addAttribute("brands", brands);
        model.addAttribute("brand", brand);
        model.addAttribute("editMode", true);
        return "product/brand";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute Brand brand, RedirectAttributes redirectAttributes) {
        try {
            brandService.addBrand(brand);
            redirectAttributes.addFlashAttribute("msg", "品牌添加成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/product/brand";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Brand brand, RedirectAttributes redirectAttributes) {
        brandService.updateBrandAndSync(brand);
        redirectAttributes.addFlashAttribute("msg", "品牌修改成功！");
        return "redirect:/product/brand";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            brandService.deleteBrand(id);
            redirectAttributes.addFlashAttribute("msg", "品牌删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/product/brand";
    }
} 