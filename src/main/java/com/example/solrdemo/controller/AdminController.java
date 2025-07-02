package com.example.solrdemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    /**
     * Solr同步管理页面
     */
    @GetMapping("/solr-sync")
    public String solrSyncPage() {
        return "admin/solr-sync";
    }
} 