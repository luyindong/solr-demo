package com.example.solrdemo.controller;

import com.example.solrdemo.dto.ProductSearchDTO;
import com.example.solrdemo.dto.ProductSearchResult;
import com.example.solrdemo.entity.Brand;
import com.example.solrdemo.entity.Category;
import com.example.solrdemo.entity.Product;
import com.example.solrdemo.entity.ProductAttribute;
import com.example.solrdemo.mapper.BrandMapper;
import com.example.solrdemo.mapper.CategoryMapper;
import com.example.solrdemo.service.ProductAttributeService;
import com.example.solrdemo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    BrandMapper brandMapper;
    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductAttributeService productAttributeService;

    /**
     * 商品列表页面
     */
    @GetMapping("/list")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) List<Long> categoryIds,
                       @RequestParam(required = false) List<Long> brandIds,
                       @RequestParam(required = false) Double minPrice,
                       @RequestParam(required = false) Double maxPrice,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "20") Integer size,
                       @RequestParam(required = false) String sortField,
                       @RequestParam(required = false) String sortOrder,
                       Model model) {

        // 构建搜索参数
        ProductSearchDTO searchDTO = new ProductSearchDTO();
        searchDTO.setKeyword(keyword);
        searchDTO.setCategoryIds(categoryIds);
        searchDTO.setBrandIds(brandIds);
        if (minPrice != null) {
            searchDTO.setMinPrice(BigDecimal.valueOf(minPrice));
        }
        if (maxPrice != null) {
            searchDTO.setMaxPrice(BigDecimal.valueOf(maxPrice));
        }
        searchDTO.setPage(page);
        searchDTO.setSize(size);
        searchDTO.setSortField(sortField);
        searchDTO.setSortOrder(sortOrder);

        // 执行搜索
        ProductSearchResult result = productService.searchProducts(searchDTO);

        // 设置模型数据
        model.addAttribute("products", result.getProducts());
        model.addAttribute("categories", result.getCategories());
        model.addAttribute("brands", result.getBrands());
        model.addAttribute("attributeFacets", result.getAttributeFacets());
        model.addAttribute("priceRange", result.getPriceRange());
        model.addAttribute("total", result.getTotal());
        model.addAttribute("page", result.getPage());
        model.addAttribute("size", result.getSize());
        model.addAttribute("totalPages", result.getTotalPages());

        // 设置搜索参数（用于页面回显）
        model.addAttribute("searchKeyword", keyword);
        model.addAttribute("searchCategoryIds", categoryIds);
        model.addAttribute("searchBrandIds", brandIds);
        model.addAttribute("searchMinPrice", minPrice);
        model.addAttribute("searchMaxPrice", maxPrice);
        model.addAttribute("searchSortField", sortField);
        model.addAttribute("searchSortOrder", sortOrder);

        return "product/list";
    }

    /**
     * AJAX搜索接口（用于动态筛选）
     */
    @PostMapping("/search")
    @ResponseBody
    public ProductSearchResult search(@RequestBody ProductSearchDTO searchDTO) {
        return productService.searchProducts(searchDTO);
    }

    @GetMapping("/add")
    public String addProductPage(Model model) {
        // 查询所有品牌和二级类目，供下拉选择
        List<Brand> brands = brandMapper.selectAll();
        List<Category> categories = categoryMapper.selectByLevel(2);
        model.addAttribute("brands", brands);
        model.addAttribute("categories", categories);
        model.addAttribute("product", new Product());
        return "product/add";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute Product product, 
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== 开始处理商品添加请求 ===");
            System.out.println("商品信息: " + product.getName() + ", 类目ID: " + product.getCategoryId());
            
            // 手动解析属性参数
            Map<String, String> attributes = new HashMap<>();
            Enumeration<String> paramNames = request.getParameterNames();
            
            System.out.println("所有参数名:");
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                System.out.println("参数: " + paramName + " = " + request.getParameter(paramName));
                
                if (paramName.startsWith("attributes[")) {
                    String attributeCode = paramName.substring(11, paramName.length() - 1); // 去掉 "attributes[" 和 "]"
                    String attributeValue = request.getParameter(paramName);
                    if (attributeValue != null && !attributeValue.trim().isEmpty()) {
                        attributes.put(attributeCode, attributeValue);
                        System.out.println("解析属性: " + attributeCode + " = " + attributeValue);
                    }
                }
            }
            
            System.out.println("解析后的属性: " + attributes);
            
            productService.addProductWithAttributes(product, attributes);
            redirectAttributes.addFlashAttribute("msg", "商品添加成功！");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "商品添加失败：" + e.getMessage());
        }
        return "redirect:/product/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            // 可自定义404页面
            return "redirect:/product/list";
        }
        
        // 查询商品属性
        List<ProductAttribute> productAttributes = productAttributeService.getAttributesByProductId(id);
        
        model.addAttribute("product", product);
        model.addAttribute("productAttributes", productAttributes);
        return "product/detail";
    }
} 