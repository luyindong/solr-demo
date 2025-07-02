package com.example.solrdemo.controller;

import com.example.solrdemo.service.SolrSyncService;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/solr")
public class SolrSyncController {

    @Autowired
    private SolrSyncService solrSyncService;
    @Autowired
    private SolrClient solrClient;

    /**
     * 手动同步所有商品数据到Solr
     */
    @PostMapping("/sync")
    public Map<String, Object> syncAllProducts() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查Solr连接
            if (!solrSyncService.checkSolrConnection()) {
                result.put("success", false);
                result.put("message", "Solr连接失败，请检查Solr服务是否启动");
                return result;
            }

            // 执行同步
            solrSyncService.syncAllProducts();

            // 获取同步后的文档数量
            long count = solrSyncService.getSolrDocumentCount();

            result.put("success", true);
            result.put("message", "数据同步成功");
            result.put("documentCount", count);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "数据同步失败: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 检查Solr连接状态
     */
    @GetMapping("/status")
    public Map<String, Object> checkStatus() {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean connected = solrSyncService.checkSolrConnection();
            result.put("connected", connected);

            if (connected) {
                long count = solrSyncService.getSolrDocumentCount();
                result.put("documentCount", count);
            }

            result.put("success", true);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "检查状态失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 清空Solr索引
     */
    @DeleteMapping("/clear")
    public Map<String, Object> clearIndex() {
        Map<String, Object> result = new HashMap<>();

        try {
            solrSyncService.clearAllProducts();
            result.put("success", true);
            result.put("message", "索引已清空");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "清空索引失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 分页查看Solr所有文档
     */
    @GetMapping("/all")
    public Map<String, Object> getAllDocs(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> result = new HashMap<>();
        try {
            SolrQuery query = new SolrQuery("*:*");
            query.setStart((page - 1) * size);
            query.setRows(size);
            QueryResponse response = solrClient.query(query);
            result.put("success", true);
            result.put("docs", response.getResults());
            result.put("total", response.getResults().getNumFound());
            result.put("page", page);
            result.put("size", size);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
} 