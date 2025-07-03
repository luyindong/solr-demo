# Solr Demo 商品系统

## 项目简介
本项目是一个基于 Spring Boot + MyBatis + Thymeleaf + Solr 的商品管理与搜索系统，支持商品、品牌、类目管理，商品高效检索与分面筛选，Solr数据同步等功能。

---

## 主要功能
- 商品管理：增删改查，支持品牌、类目、价格、关键词等多条件筛选
- 品牌管理：增删改查，支持状态切换，编辑时自动同步商品冗余字段与Solr索引
- 类目管理：增删改查，支持状态切换，编辑时自动同步商品冗余字段与Solr索引
- 类目属性管理：支持为不同类目定义自定义属性（文本、数字、选择、范围），实现动态筛选
- 商品属性管理：支持为商品设置具体的属性值
- Solr数据同步：支持全量同步、单商品同步、分面统计联动
- 分面筛选：商品列表页支持品牌、类目、价格、动态属性等多维度筛选
- 前后端分离友好，支持AJAX动态搜索

---

## 开发环境
- JDK 8 及以上
- Maven 3.6+
- MySQL 5.7/8.0
- Apache Solr 8.11.x
- 推荐IDE：IntelliJ IDEA

---

## 主要技术栈
- Spring Boot 2.7.x
- MyBatis
- Thymeleaf
- Apache Solr (solr-solrj)
- Bootstrap 5 + jQuery

---

## 目录结构
```
solr-demo/
├── pom.xml
├── readme.md
├── src/
│   ├── main/
│   │   ├── java/com/example/solrdemo/
│   │   │   ├── controller/         # 控制器（商品、品牌、类目、Solr同步）
│   │   │   ├── dto/                # DTO对象
│   │   │   ├── entity/             # 实体类
│   │   │   ├── mapper/             # MyBatis Mapper接口
│   │   │   ├── service/            # 业务服务（含Solr同步、品牌、类目等）
│   │   │   └── SolrDemoApplication.java
│   │   ├── resources/
│   │   │   ├── mapper/             # MyBatis XML
│   │   │   ├── static/             # 静态资源
│   │   │   └── templates/          # Thymeleaf模板
│   └── test/
└── target/
```

---

## 启动方式
1. **准备数据库和Solr**
   - 创建MySQL数据库，导入表结构和初始数据（参考src/main/resources/db/schema.sql）
   - 启动Solr服务，创建对应的core（如product），schema需包含所有商品字段
   - ```bash
        <field name="brand_id" type="pint" uninvertible="true" indexed="true" stored="true"/>
        <field name="brand_id_name" type="string" uninvertible="true" multiValued="true" indexed="true" stored="true"/>
        <field name="brand_name" type="string" uninvertible="true" indexed="true" stored="true"/>
        <field name="category_id" type="pint" uninvertible="true" indexed="true" stored="true"/>
        <field name="category_id_name" type="string" uninvertible="true" multiValued="true" indexed="true" stored="true"/>
        <field name="category_name" type="string" uninvertible="true" indexed="true" stored="true"/>
        <field name="create_time" type="pdate" uninvertible="true" indexed="true" stored="true"/>
        <field name="description" type="string" uninvertible="true" indexed="true" stored="true"/>
        <field name="id" type="string" multiValued="false" indexed="true" required="true" stored="true"/>
        <field name="image" type="string" uninvertible="true" indexed="true" stored="true"/>
        <field name="name" type="string" uninvertible="true" indexed="true" stored="true"/>
        <field name="price" type="string" uninvertible="true" indexed="true" stored="true"/>
        <field name="status" type="pint" uninvertible="true" indexed="true" stored="true"/>
        <field name="stock" type="pint" uninvertible="true" indexed="true" stored="true"/>
        <field name="update_time" type="text_general"/>
     ```
2. **配置数据库和Solr连接**
   - 在`src/main/resources/application-dev.properties`中配置MySQL和Solr地址
3. **编译并启动项目**
   ```bash
   mvn clean package
   java -jar target/solr-demo-0.0.1-SNAPSHOT.jar
   ```
4. **访问系统**
   - 商品列表：http://localhost:8080/product/list
   - 品牌管理：http://localhost:8080/product/brand
   - 类目管理：http://localhost:8080/product/category
   - 类目属性管理：http://localhost:8080/product/attribute/list?categoryId=1
   - 属性功能测试：http://localhost:8080/product/test-attribute
   - 属性同步测试：http://localhost:8080/product/test-attribute-sync
   - Solr同步管理：http://localhost:8080/admin/solr-sync

---

## 核心功能说明
### 1. Solr数据同步
- 支持全量同步、单商品同步、分面统计联动
- 商品、品牌、类目变更时自动同步Solr索引
- 分面字段如`brand_id_name`、`category_id_name`采用"ID::名称"拼接，前端和后端均可正确解析
- Solr数据同步管理页面支持Solr文档分页查看、上一页/下一页、输入页码跳转等功能，便于调试和数据核查

### 2. 品牌/类目管理
- 增删改查，支持状态切换（启用/禁用）
- 删除前校验是否有商品绑定，防止误删
- 编辑时自动同步商品表冗余字段和Solr索引
- 表单支持编辑回显、模式切换

### 3. 商品搜索与分面筛选
- 支持关键词、品牌、类目、价格区间等多条件组合筛选
- 支持动态属性筛选：根据选择的类目动态加载对应的属性筛选选项
- 属性筛选支持多种类型：文本输入、数字范围、多选选项等
- Solr分面统计实现品牌、类目筛选项联动，前端AJAX动态渲染
- 商品详情页展示品牌、类目信息

### 4. 动态属性系统
- 类目属性管理：为每个类目定义自定义属性，支持文本、数字、选择、范围四种类型
- 商品属性管理：为商品设置具体的属性值，支持批量操作
- 商品添加页面：选择类目后动态加载属性填写表单
- 商品详情页面：展示商品的所有属性信息
- 动态筛选：根据选择的类目自动加载对应的属性筛选选项
- 属性值支持多种格式：单选、多选、范围查询等
- 属性同步到Solr：商品属性自动同步到Solr索引，字段命名规则为`attr_{attributeCode}`，支持属性筛选和全文搜索

---

## 常见问题
- **Q: 启动报Solr连接失败？**
  - 检查Solr服务是否启动，application.properties中的solr.url是否正确
- **Q: 页面报模板找不到？**
  - 确认templates目录下相关html文件存在，Controller返回路径与文件名一致
- **Q: 品牌/类目编辑后商品信息未同步？**
  - 检查Service层是否调用了同步方法，Solr服务是否正常

---

## 联系与贡献
如有建议或问题，欢迎提issue或PR