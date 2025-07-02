package com.example.solrdemo.service;

import com.example.solrdemo.entity.Product;
import com.example.solrdemo.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SolrSyncService {

    private final static String Separator = "::";
    @Autowired
    private SolrClient solrClient;
    @Autowired
    private ProductMapper productMapper;

    public void clearAllProducts() throws IOException, SolrServerException {
        // 清空Solr索引
        solrClient.deleteByQuery("*:*");
        solrClient.commit();
    }

    /**
     * 同步所有商品数据到Solr
     */
    public void syncAllProducts() throws IOException, SolrServerException {
        // 清空Solr索引
        solrClient.deleteByQuery("*:*");
        solrClient.commit();

        // 分批同步数据
        int pageSize = 100;
        int page = 1;
        boolean hasMore = true;

        while (hasMore) {
            List<Product> products = getProductsByPage(page, pageSize);
            if (products.isEmpty()) {
                hasMore = false;
            } else {
                syncProductsToSolr(products);
                page++;
            }
        }

        // 提交并优化索引
        solrClient.commit();
        solrClient.optimize();

        System.out.println("数据同步完成！");
    }

    /**
     * 同步单个商品到Solr
     */
    public void syncProduct(Product product) throws IOException, SolrServerException {
        SolrInputDocument doc = convertProductToSolrDoc(product);
        solrClient.add(doc);
        solrClient.commit();
        log.info("sync product: " + product);
    }

    /**
     * 更新商品到Solr
     */
    public void updateProduct(Product product) throws IOException, SolrServerException {
        // 先删除旧数据
        solrClient.deleteByQuery("id:" + product.getId());
        // 添加新数据
        syncProduct(product);
    }

    /**
     * 从Solr删除商品
     */
    public void deleteProduct(Long productId) throws IOException, SolrServerException {
        solrClient.deleteByQuery("id:" + productId);
        solrClient.commit();
    }

    /**
     * 分页获取商品数据
     */
    private List<Product> getProductsByPage(int page, int pageSize) {
        // 这里需要修改ProductMapper，添加分页查询方法
        // 或者使用现有的查询方法，手动分页
        List<Product> allProducts = productMapper.selectAll();

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, allProducts.size());

        if (start >= allProducts.size()) {
            return new ArrayList<>();
        }

        return allProducts.subList(start, end);
    }

    /**
     * 批量同步商品到Solr
     */
    private void syncProductsToSolr(List<Product> products) throws IOException, SolrServerException {
        List<SolrInputDocument> docs = new ArrayList<>();

        for (Product product : products) {
            SolrInputDocument doc = convertProductToSolrDoc(product);
            docs.add(doc);
        }

        solrClient.add(docs);
        System.out.println("同步了 " + products.size() + " 条商品数据");
    }

    /**
     * 将Product对象转换为Solr文档
     */
    public SolrInputDocument convertProductToSolrDoc(Product product) {
        SolrInputDocument doc = new SolrInputDocument();

        doc.addField("id", product.getId());
        doc.addField("name", product.getName());
        doc.addField("category_id_name", product.getCategoryId() + Separator + product.getCategoryName());
        doc.addField("category_id", product.getCategoryId());
        doc.addField("category_name", product.getCategoryName());
        doc.addField("brand_id_name", product.getBrandId() + Separator + product.getBrandName());
        doc.addField("brand_id", product.getBrandId());
        doc.addField("brand_name", product.getBrandName());
        doc.addField("price", product.getPrice().toString());
        doc.addField("description", product.getDescription());
        doc.addField("image", product.getImage());
        doc.addField("stock", product.getStock());
        doc.addField("status", product.getStatus());
        doc.addField("create_time", product.getCreateTime().atZone(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ISO_INSTANT));
        return doc;
    }

    /**
     * 检查Solr连接状态
     */
    public boolean checkSolrConnection() {
        try {
            solrClient.ping();
            return true;
        } catch (Exception e) {
            System.err.println("Solr连接失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取Solr中的文档数量
     */
    public long getSolrDocumentCount() throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery("*:*");
        return solrClient.query(query).getResults().getNumFound();
    }
} 