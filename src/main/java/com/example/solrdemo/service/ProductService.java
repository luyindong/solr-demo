package com.example.solrdemo.service;

import com.example.solrdemo.dto.ProductSearchDTO;
import com.example.solrdemo.dto.ProductSearchResult;
import com.example.solrdemo.entity.*;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private CategoryAttributeService categoryAttributeService;
    @Autowired
    private ProductAttributeService productAttributeService;

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
        // 属性筛选
        addAttributeFiltersToQuery(q, searchDTO);

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

        // 添加属性分面查询
        addAttributeFacets(query, searchDTO);

        query.setFacetMinCount(1);

        return query;
    }

    /**
     * 添加属性筛选条件到查询
     */
    private void addAttributeFiltersToQuery(StringBuilder q, ProductSearchDTO searchDTO) {
        // 处理复杂属性筛选（支持单选、多选、范围）
        if (searchDTO.getAttributes() != null && !searchDTO.getAttributes().isEmpty()) {
            for (Map.Entry<String, Object> entry : searchDTO.getAttributes().entrySet()) {
                String attributeCode = entry.getKey();
                Object attributeValue = entry.getValue();

                if (attributeValue instanceof List) {
                    // 多选值
                    @SuppressWarnings("unchecked")
                    List<String> values = (List<String>) attributeValue;
                    if (!values.isEmpty()) {
                        q.append(" AND attr_").append(attributeCode).append(":(");
                        for (int i = 0; i < values.size(); i++) {
                            if (i > 0) q.append(" OR ");
                            q.append("\"").append(values.get(i)).append("\"");
                        }
                        q.append(")");
                        log.info("添加多选属性筛选: attr_{} = {}", attributeCode, values);
                    }
                } else if (attributeValue instanceof Map) {
                    // 范围值
                    @SuppressWarnings("unchecked")
                    Map<String, Object> range = (Map<String, Object>) attributeValue;
                    String minValue = (String) range.get("min");
                    String maxValue = (String) range.get("max");
                    if (minValue != null || maxValue != null) {
                        q.append(" AND attr_").append(attributeCode).append(":[");
                        q.append(minValue != null ? minValue : "*");
                        q.append(" TO ");
                        q.append(maxValue != null ? maxValue : "*");
                        q.append("]");
                        log.info("添加范围属性筛选: attr_{} = [{} TO {}]", attributeCode, minValue, maxValue);
                    }
                } else {
                    // 单选值
                    String value = attributeValue != null ? attributeValue.toString() : null;
                    if (value != null && !value.isEmpty()) {
                        q.append(" AND attr_").append(attributeCode).append(":\"").append(value).append("\"");
                        log.info("添加单选属性筛选: attr_{} = {}", attributeCode, value);
                    }
                }
            }
        }
    }

    /**
     * 添加属性分面查询
     */
    private void addAttributeFacets(SolrQuery query, ProductSearchDTO searchDTO) {
        // 如果有类目筛选，获取该类目的属性定义
        if (searchDTO.getCategoryIds() != null && !searchDTO.getCategoryIds().isEmpty()) {
            Long categoryId = searchDTO.getCategoryIds().get(0); // 取第一个类目
            List<CategoryAttribute> attributes = categoryAttributeService.getAttributesByCategoryId(categoryId);

            for (CategoryAttribute attribute : attributes) {
                String fieldName = "attr_" + attribute.getAttributeCode();
                query.addFacetField(fieldName);
                log.info("添加属性分面字段: {}", fieldName);
            }
        }
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

        // 属性分面
        setAttributeFacets(result, searchDTO, response);
    }

    /**
     * 设置属性分面结果
     */
    private void setAttributeFacets(ProductSearchResult result, ProductSearchDTO searchDTO, QueryResponse response) {
        // 如果有类目筛选，获取该类目的属性定义
        if (searchDTO.getCategoryIds() != null && !searchDTO.getCategoryIds().isEmpty()) {
            Long categoryId = searchDTO.getCategoryIds().get(0);
            List<CategoryAttribute> attributes = categoryAttributeService.getAttributesByCategoryId(categoryId);

            Map<String, List<ProductSearchResult.AttributeOption>> attributeFacets = new HashMap<>();

            for (CategoryAttribute attribute : attributes) {
                String fieldName = "attr_" + attribute.getAttributeCode();
                List<ProductSearchResult.AttributeOption> options = new ArrayList<>();

                if (response.getFacetField(fieldName) != null) {
                    // 用于合并被分词的属性值
                    Map<String, Long> valueCountMap = new HashMap<>();

                    for (var count : response.getFacetField(fieldName).getValues()) {
                        String value = count.getName();
                        Long countValue = count.getCount();

                        // 尝试合并被分词的属性值
                        boolean merged = false;
                        String targetKey = null;

                        for (String existingValue : valueCountMap.keySet()) {
                            // 检查是否应该合并
                            if (shouldMergeAttributeValues(existingValue, value)) {
                                targetKey = existingValue;
                                merged = true;
                                break;
                            }
                        }

                        if (merged && targetKey != null) {
                            // 合并到更长的值上
                            valueCountMap.put(targetKey, valueCountMap.get(targetKey) + countValue);
                        } else {
                            valueCountMap.put(value, countValue);
                        }
                    }

                    // 转换为AttributeOption
                    for (Map.Entry<String, Long> entry : valueCountMap.entrySet()) {
                        ProductSearchResult.AttributeOption option = new ProductSearchResult.AttributeOption();
                        option.setCode(attribute.getAttributeCode());
                        option.setName(attribute.getAttributeName());
                        option.setValue(entry.getKey());
                        option.setCount(entry.getValue());
                        options.add(option);
                    }
                }

                if (!options.isEmpty()) {
                    attributeFacets.put(attribute.getAttributeCode(), options);
                }
            }

            log.info("获取分面字段 {}", attributeFacets);
            result.setAttributeFacets(attributeFacets);
        }
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

    /**
     * 添加商品并保存属性
     */
    @Transactional
    public void addProductWithAttributes(Product product, Map<String, String> attributes) {
        log.info("开始添加商品: {}", product.getName());
        log.info("接收到的属性数据: {}", attributes);

        // 添加商品基本信息
        addProduct(product);
        log.info("商品基本信息保存成功，ID: {}", product.getId());

        // 处理属性数据
        if (attributes != null && !attributes.isEmpty()) {
            List<ProductAttribute> productAttributes = new ArrayList<>();

            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                String attributeCode = entry.getKey();
                String attributeValue = entry.getValue();

                log.info("处理属性: {} = {}", attributeCode, attributeValue);

                // 跳过空值
                if (attributeValue == null || attributeValue.trim().isEmpty()) {
                    log.info("跳过空属性: {}", attributeCode);
                    continue;
                }

                // 查询属性定义
                CategoryAttribute categoryAttribute = categoryAttributeService.getAttributeByCode(product.getCategoryId(), attributeCode);
                if (categoryAttribute != null) {
                    ProductAttribute productAttribute = new ProductAttribute();
                    productAttribute.setProductId(product.getId());
                    productAttribute.setAttributeId(categoryAttribute.getId());
                    productAttribute.setAttributeCode(attributeCode);
                    productAttribute.setAttributeName(categoryAttribute.getAttributeName());
                    productAttribute.setAttributeValue(attributeValue);
                    productAttribute.setAttributeType(categoryAttribute.getAttributeType());

                    productAttributes.add(productAttribute);
                    log.info("准备保存属性: {} = {}", attributeCode, attributeValue);
                } else {
                    log.warn("未找到属性定义: categoryId={}, attributeCode={}", product.getCategoryId(), attributeCode);
                }
            }

            // 批量保存属性
            if (!productAttributes.isEmpty()) {
                productAttributeService.batchSaveAttributes(productAttributes);
                log.info("成功保存 {} 个属性", productAttributes.size());
            } else {
                log.info("没有属性需要保存");
            }
        } else {
            log.info("没有接收到属性数据");
        }

        // 同步商品和属性到Solr
        try {
            solrSyncService.syncProduct(product);
            log.info("商品和属性同步到Solr成功");
        } catch (Exception e) {
            log.error("同步到Solr失败", e);
        }
    }


    /**
     * 判断两个属性值是否应该合并
     * 用于处理Solr分词导致的属性值拆分问题
     */
    private boolean shouldMergeAttributeValues(String value1, String value2) {
        // 如果两个值完全相同，不需要合并
        if (value1.equals(value2)) {
            return false;
        }

        // 如果一个值包含另一个值，应该合并
        if (value1.contains(value2) || value2.contains(value1)) {
            return true;
        }

        // 对于中文属性值，检查是否是分词结果
        // 例如："红色" 被分词为 "红" 和 "色"
        if (value1.length() == 1 && value2.length() == 1) {
            // 两个单字符，可能是分词结果
            return true;
        }

        // 检查是否是常见的分词模式
        String[] commonPatterns = {
                "红色", "蓝色", "绿色", "黄色", "黑色", "白色",
                "大号", "中号", "小号", "特大", "特小",
                "棉质", "丝绸", "皮革", "塑料", "金属"
        };

        for (String pattern : commonPatterns) {
            if (pattern.contains(value1) && pattern.contains(value2)) {
                return true;
            }
        }

        return false;
    }
}