package com.example.solrdemo.service;

import com.example.solrdemo.dto.ProductSearchDTO;
import com.example.solrdemo.dto.ProductSearchResult;
import com.example.solrdemo.entity.Brand;
import com.example.solrdemo.entity.Category;
import com.example.solrdemo.entity.Product;
import com.example.solrdemo.mapper.BrandMapper;
import com.example.solrdemo.mapper.CategoryMapper;
import com.example.solrdemo.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ProductService {
    @Autowired
    SolrSyncService solrSyncService;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private SolrClient solrClient;

    /**
     * 搜索商品（使用Solr）
     */
    public ProductSearchResult searchProducts(ProductSearchDTO searchDTO) {
        ProductSearchResult result = new ProductSearchResult();

        try {
            // 构建Solr查询
            SolrQuery query = buildSolrQuery(searchDTO);

            // 执行查询
            QueryResponse response = solrClient.query(query);
            SolrDocumentList documents = response.getResults();

            // 转换结果
            List<Product> products = convertSolrDocumentsToProducts(documents);
            result.setProducts(products);
            result.setTotal(documents.getNumFound());
            result.setPage(searchDTO.getPage());
            result.setSize(searchDTO.getSize());
            result.setTotalPages((int) Math.ceil((double) documents.getNumFound() / searchDTO.getSize()));

            // 获取筛选选项
            setFilterOptions(result, searchDTO, response);

        } catch (Exception e) {
            e.printStackTrace();
            // 如果Solr查询失败，回退到数据库查询
            log.error("Solr查询失败，回退到数据库查询");
            return searchProductsFromDB(searchDTO);
        }

        return result;
    }

    /**
     * 从数据库搜索商品（备用方案）
     */
    public ProductSearchResult searchProductsFromDB(ProductSearchDTO searchDTO) {
        ProductSearchResult result = new ProductSearchResult();

        // 查询商品列表
        List<Product> products = productMapper.selectBySearch(searchDTO);
        result.setProducts(products);

        // 查询总数
        Long total = productMapper.countBySearch(searchDTO);
        result.setTotal(total);
        result.setPage(searchDTO.getPage());
        result.setSize(searchDTO.getSize());
        result.setTotalPages((int) Math.ceil((double) total / searchDTO.getSize()));

        // 设置筛选选项
        setFilterOptionsFromDB(result, searchDTO);

        return result;
    }

    /**
     * 构建Solr查询
     */
    private SolrQuery buildSolrQuery(ProductSearchDTO searchDTO) {
        SolrQuery query = new SolrQuery();

        // 基础查询
        StringBuilder q = new StringBuilder("status:1");

        // 关键词搜索
        if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
            q.append(" AND (name:*").append(searchDTO.getKeyword()).append("* OR description:*")
                    .append(searchDTO.getKeyword()).append("*)");
        }

        // 类目筛选
        if (searchDTO.getCategoryIds() != null && !searchDTO.getCategoryIds().isEmpty()) {
            q.append(" AND category_id:(");
            for (int i = 0; i < searchDTO.getCategoryIds().size(); i++) {
                if (i > 0) q.append(" OR ");
                q.append(searchDTO.getCategoryIds().get(i));
            }
            q.append(")");
        }

        // 品牌筛选
        if (searchDTO.getBrandIds() != null && !searchDTO.getBrandIds().isEmpty()) {
            q.append(" AND brand_id:(");
            for (int i = 0; i < searchDTO.getBrandIds().size(); i++) {
                if (i > 0) q.append(" OR ");
                q.append(searchDTO.getBrandIds().get(i));
            }
            q.append(")");
        }

        // 价格筛选
        if (searchDTO.getMinPrice() != null) {
            q.append(" AND price:[").append(searchDTO.getMinPrice()).append(" TO *]");
        }
        if (searchDTO.getMaxPrice() != null) {
            q.append(" AND price:[* TO ").append(searchDTO.getMaxPrice()).append("]");
        }

        query.setQuery(q.toString());

        // 分页
        query.setStart(searchDTO.getOffset());
        query.setRows(searchDTO.getSize());

        // 排序
        if (searchDTO.getSortField() != null && !searchDTO.getSortField().isEmpty()) {
            query.setSort(searchDTO.getSortField(),
                    "desc".equalsIgnoreCase(searchDTO.getSortOrder()) ?
                            SolrQuery.ORDER.desc : SolrQuery.ORDER.asc);
        } else {
            query.setSort("create_time", SolrQuery.ORDER.desc);
        }

        // 添加facet分面查询
        query.setFacet(true);
        query.addFacetField("category_id_name", "brand_id_name");
        query.setFacetMinCount(1);

        return query;
    }

    /**
     * 转换Solr文档为Product对象
     */
    private List<Product> convertSolrDocumentsToProducts(SolrDocumentList documents) {
        List<Product> products = new ArrayList<>();

        for (SolrDocument doc : documents) {
            Product product = new Product();
            product.setId(Long.valueOf(doc.get("id").toString()));
            product.setName((String) doc.get("name"));
            product.setCategoryId(Long.valueOf(doc.get("category_id").toString()));
            product.setCategoryName((String) doc.get("category_name"));
            product.setBrandId(Long.valueOf(doc.get("brand_id").toString()));
            product.setBrandName((String) doc.get("brand_name"));
            product.setPrice(new BigDecimal(doc.get("price").toString()));
            product.setDescription((String) doc.get("description"));
            product.setImage((String) doc.get("image"));
            product.setStock(Integer.valueOf(doc.get("stock").toString()));
            product.setStatus(Integer.valueOf(doc.get("status").toString()));

            products.add(product);
        }

        return products;
    }

    /**
     * 设置筛选选项（从Solr facet结果）
     */
    private void setFilterOptions(ProductSearchResult result, ProductSearchDTO searchDTO, QueryResponse response) {
        // 类目分面
        List<ProductSearchResult.CategoryOption> categories = new ArrayList<>();
        if (response.getFacetField("category_id_name") != null) {
            for (var count : response.getFacetField("category_id_name").getValues()) {
                ProductSearchResult.CategoryOption option = new ProductSearchResult.CategoryOption();
                String[] parts = count.getName().split("::", 2);
                option.setId(Long.valueOf(parts[0]));
                option.setName(parts.length > 1 ? parts[1] : "");
                option.setCount(count.getCount());
                categories.add(option);
            }
        }
        result.setCategories(categories);

        // 品牌分面
        List<ProductSearchResult.BrandOption> brands = new ArrayList<>();
        if (response.getFacetField("brand_id_name") != null) {
            for (var count : response.getFacetField("brand_id_name").getValues()) {
                ProductSearchResult.BrandOption option = new ProductSearchResult.BrandOption();
                String[] parts = count.getName().split("::", 2);
                option.setId(Long.valueOf(parts[0]));
                option.setName(parts.length > 1 ? parts[1] : "");
                option.setCount(count.getCount());
                brands.add(option);
            }
        }
        result.setBrands(brands);
    }

    /**
     * 设置筛选选项（从数据库）
     */
    private void setFilterOptionsFromDB(ProductSearchResult result, ProductSearchDTO searchDTO) {
        // 获取所有二级类目
        List<Category> categories = categoryMapper.selectByLevel(2);
        List<ProductSearchResult.CategoryOption> categoryOptions = new ArrayList<>();
        for (Category category : categories) {
            ProductSearchResult.CategoryOption option = new ProductSearchResult.CategoryOption();
            option.setId(category.getId());
            option.setName(category.getName());
            // 这里可以添加统计逻辑
            option.setCount(1L);
            categoryOptions.add(option);
        }
        result.setCategories(categoryOptions);

        // 获取所有品牌
        List<Brand> brands = brandMapper.selectByStatus(1);
        List<ProductSearchResult.BrandOption> brandOptions = new ArrayList<>();
        for (Brand brand : brands) {
            ProductSearchResult.BrandOption option = new ProductSearchResult.BrandOption();
            option.setId(brand.getId());
            option.setName(brand.getName());
            // 这里可以添加统计逻辑
            option.setCount(1L);
            brandOptions.add(option);
        }
        result.setBrands(brandOptions);
    }

    public void addProduct(Product product) {
        // 冗余字段赋值
        Brand brand = brandMapper.selectById(product.getBrandId());
        Category category = categoryMapper.selectById(product.getCategoryId());
        if (brand != null) product.setBrandName(brand.getName());
        if (category != null) product.setCategoryName(category.getName());
        product.setStatus(1); // 默认上架
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product);

        try {
            solrSyncService.syncProduct(product);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }
}