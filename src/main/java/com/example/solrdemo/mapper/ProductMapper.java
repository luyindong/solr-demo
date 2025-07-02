package com.example.solrdemo.mapper;

import com.example.solrdemo.dto.ProductSearchDTO;
import com.example.solrdemo.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    List<Product> selectAll();

    Product selectById(@Param("id") Long id);

    List<Product> selectBySearch(ProductSearchDTO searchDTO);

    Long countBySearch(ProductSearchDTO searchDTO);

    List<Product> selectByCategoryIds(@Param("categoryIds") List<Long> categoryIds);

    List<Product> selectByBrandIds(@Param("brandIds") List<Long> brandIds);

    int insert(Product product);

    int update(Product product);

    int deleteById(@Param("id") Long id);

    int updateBrandNameByBrandId(@Param("brandId") Long brandId, @Param("brandName") String brandName);

    int updateCategoryNameByCategoryId(@Param("categoryId") Long categoryId, @Param("categoryName") String categoryName);
} 