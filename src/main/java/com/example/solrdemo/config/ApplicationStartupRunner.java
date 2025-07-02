package com.example.solrdemo.config;

import com.example.solrdemo.service.SolrSyncService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;

@AllArgsConstructor
public class ApplicationStartupRunner implements CommandLineRunner {

    private SolrSyncService solrSyncService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("应用启动，开始检查Solr数据同步状态...");

        try {
            // 检查Solr连接
            if (!solrSyncService.checkSolrConnection()) {
                System.err.println("警告: Solr连接失败，请检查Solr服务是否启动在 http://127.0.0.1:8983/solr/demo");
                System.err.println("应用将继续启动，但搜索功能可能不可用");
                return;
            }

            // 检查Solr中是否有数据
            long documentCount = solrSyncService.getSolrDocumentCount();

            if (documentCount == 0) {
                System.out.println("Solr中没有数据，开始同步MySQL数据到Solr...");
                solrSyncService.syncAllProducts();
                System.out.println("数据同步完成！");
            } else {
                System.out.println("Solr中已有 " + documentCount + " 条数据，跳过同步");
            }

        } catch (Exception e) {
            System.err.println("数据同步过程中发生错误: " + e.getMessage());
            System.err.println("应用将继续启动，但搜索功能可能不可用");
            e.printStackTrace();
        }
    }
} 