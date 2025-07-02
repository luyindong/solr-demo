package com.example.solrdemo.service;

import com.example.solrdemo.entity.Category;
import com.example.solrdemo.entity.Product;
import com.example.solrdemo.mapper.CategoryMapper;
import com.example.solrdemo.mapper.ProductMapper;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private SolrSyncService solrSyncService;
    @Autowired
    private SolrClient solrClient;

    public void updateCategoryAndSync(Category category) {
        categoryMapper.update(category);
        // 1. 批量更新数据库
        productMapper.updateCategoryNameByCategoryId(category.getId(), category.getName());
        // 2. 批量同步Solr
        List<Product> products = productMapper.selectByCategoryIds(Collections.singletonList(category.getId()));
        List<SolrInputDocument> docs = new ArrayList<>();
        for (Product product : products) {
            docs.add(solrSyncService.convertProductToSolrDoc(product));
        }
        try {
            if (!docs.isEmpty()) {
                solrClient.add(docs);
                solrClient.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addCategory(Category category) {
        category.setStatus(1);
        categoryMapper.insert(category);
    }

    public void deleteCategory(Long id) {
        // 校验是否有商品绑定
        if (!productMapper.selectByCategoryIds(Collections.singletonList(id)).isEmpty()) {
            throw new RuntimeException("该类目下存在商品，无法删除！");
        }
        categoryMapper.deleteById(id);
    }
} 