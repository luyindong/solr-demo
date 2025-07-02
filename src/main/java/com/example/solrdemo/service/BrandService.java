package com.example.solrdemo.service;

import com.example.solrdemo.entity.Brand;
import com.example.solrdemo.entity.Product;
import com.example.solrdemo.mapper.BrandMapper;
import com.example.solrdemo.mapper.ProductMapper;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private SolrSyncService solrSyncService;
    @Autowired
    private SolrClient solrClient;

    public void updateBrandAndSync(Brand brand) {
        brandMapper.update(brand);
        // 1. 批量更新数据库
        productMapper.updateBrandNameByBrandId(brand.getId(), brand.getName());
        // 2. 批量同步Solr
        List<Product> products = productMapper.selectByBrandIds(Collections.singletonList(brand.getId()));
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

    public void addBrand(Brand brand) {
        brand.setStatus(1);
        brandMapper.insert(brand);
    }

    public void deleteBrand(Long id) {
        // 校验是否有商品绑定
        if (!productMapper.selectByBrandIds(Collections.singletonList(id)).isEmpty()) {
            throw new RuntimeException("该品牌下存在商品，无法删除！");
        }
        brandMapper.deleteById(id);
    }
} 