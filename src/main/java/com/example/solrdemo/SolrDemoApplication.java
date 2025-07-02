package com.example.solrdemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.solrdemo.mapper")
public class SolrDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SolrDemoApplication.class, args);
    }

} 