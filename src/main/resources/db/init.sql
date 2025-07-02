-- 品牌表
CREATE TABLE `brand`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '品牌ID',
    `name`        VARCHAR(64) NOT NULL COMMENT '品牌名称',
    `logo`        VARCHAR(255) DEFAULT NULL COMMENT '品牌LOGO',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '品牌描述',
    `sort`        INT          DEFAULT 0 COMMENT '排序',
    `status`      TINYINT      DEFAULT 1 COMMENT '0-禁用 1-启用',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='品牌表';

-- 类目表（支持多级，parent_id=0为一级类目，parent_id>0为二级类目）
CREATE TABLE `category`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '类目ID',
    `name`        VARCHAR(64) NOT NULL COMMENT '类目名称',
    `parent_id`   BIGINT   DEFAULT 0 COMMENT '父类目ID',
    `level`       INT      DEFAULT 2 COMMENT '类目层级 1-一级 2-二级',
    `sort`        INT      DEFAULT 0 COMMENT '排序',
    `status`      TINYINT  DEFAULT 1 COMMENT '0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='类目表';

-- 商品表
CREATE TABLE `product`
(
    `id`            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    `name`          VARCHAR(128)   NOT NULL COMMENT '商品名称',
    `category_id`   BIGINT         NOT NULL COMMENT '类目ID',
    `category_name` VARCHAR(64)    NOT NULL COMMENT '冗余类目名',
    `brand_id`      BIGINT         NOT NULL COMMENT '品牌ID',
    `brand_name`    VARCHAR(64)    NOT NULL COMMENT '冗余品牌名',
    `price`         DECIMAL(10, 2) NOT NULL COMMENT '价格',
    `description`   VARCHAR(255) DEFAULT NULL COMMENT '商品描述',
    `image`         VARCHAR(255) DEFAULT NULL COMMENT '商品图片',
    `stock`         INT          DEFAULT 0 COMMENT '库存',
    `status`        TINYINT      DEFAULT 1 COMMENT '0-下架 1-上架',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商品表';


-- 插入品牌数据
INSERT INTO `brand` (`id`, `name`, `logo`, `description`, `sort`, `status`)
VALUES (1, '苹果', '/images/brands/apple.png', 'Apple Inc.', 1, 1),
       (2, '华为', '/images/brands/huawei.png', '华为技术有限公司', 2, 1),
       (3, '小米', '/images/brands/xiaomi.png', '小米科技有限责任公司', 3, 1),
       (4, '三星', '/images/brands/samsung.png', 'Samsung Electronics', 4, 1),
       (5, 'OPPO', '/images/brands/oppo.png', 'OPPO广东移动通信有限公司', 5, 1),
       (6, 'vivo', '/images/brands/vivo.png', '维沃移动通信有限公司', 6, 1),
       (7, '联想', '/images/brands/lenovo.png', '联想集团有限公司', 7, 1),
       (8, '戴尔', '/images/brands/dell.png', 'Dell Technologies', 8, 1);

-- 插入类目数据（一级类目）
INSERT INTO `category` (`id`, `name`, `parent_id`, `level`, `sort`, `status`)
VALUES (1, '手机数码', 0, 1, 1, 1),
       (2, '电脑办公', 0, 1, 2, 1),
       (3, '家用电器', 0, 1, 3, 1),
       (4, '服装鞋帽', 0, 1, 4, 1);

-- 插入类目数据（二级类目）
INSERT INTO `category` (`id`, `name`, `parent_id`, `level`, `sort`, `status`)
VALUES (11, '智能手机', 1, 2, 1, 1),
       (12, '平板电脑', 1, 2, 2, 1),
       (13, '智能手表', 1, 2, 3, 1),
       (14, '耳机音响', 1, 2, 4, 1),
       (21, '笔记本电脑', 2, 2, 1, 1),
       (22, '台式电脑', 2, 2, 2, 1),
       (23, '办公设备', 2, 2, 3, 1),
       (24, '网络设备', 2, 2, 4, 1),
       (31, '电视', 3, 2, 1, 1),
       (32, '冰箱', 3, 2, 2, 1),
       (33, '洗衣机', 3, 2, 3, 1),
       (34, '空调', 3, 2, 4, 1),
       (41, '男装', 4, 2, 1, 1),
       (42, '女装', 4, 2, 2, 1),
       (43, '运动鞋', 4, 2, 3, 1),
       (44, '箱包', 4, 2, 4, 1);

-- 插入商品数据
INSERT INTO `product` (`name`, `category_id`, `category_name`, `brand_id`, `brand_name`, `price`, `description`,
                       `image`, `stock`, `status`)
VALUES
-- 智能手机
('iPhone 15 Pro', 11, '智能手机', 1, '苹果', 7999.00, 'iPhone 15 Pro 256GB 深空黑色',
 '/images/products/iphone15pro.jpg', 100, 1),
('iPhone 15', 11, '智能手机', 1, '苹果', 5999.00, 'iPhone 15 128GB 蓝色', '/images/products/iphone15.jpg', 150, 1),
('华为 Mate 60 Pro', 11, '智能手机', 2, '华为', 6999.00, '华为 Mate 60 Pro 512GB 雅川青',
 '/images/products/mate60pro.jpg', 80, 1),
('小米 14 Pro', 11, '智能手机', 3, '小米', 4999.00, '小米 14 Pro 256GB 钛金属黑', '/images/products/mi14pro.jpg', 200,
 1),
('三星 Galaxy S24', 11, '智能手机', 4, '三星', 6999.00, '三星 Galaxy S24 256GB 钛金灰',
 '/images/products/galaxys24.jpg', 120, 1),
('OPPO Find X7', 11, '智能手机', 5, 'OPPO', 5999.00, 'OPPO Find X7 256GB 海阔天空', '/images/products/findx7.jpg', 90,
 1),

-- 平板电脑
('iPad Pro 12.9', 12, '平板电脑', 1, '苹果', 8999.00, 'iPad Pro 12.9英寸 256GB 深空灰色',
 '/images/products/ipadpro.jpg', 50, 1),
('华为 MatePad Pro', 12, '平板电脑', 2, '华为', 4999.00, '华为 MatePad Pro 11英寸 256GB 雅川青',
 '/images/products/matepadpro.jpg', 60, 1),
('小米平板 6 Pro', 12, '平板电脑', 3, '小米', 2999.00, '小米平板 6 Pro 11英寸 256GB 深空灰',
 '/images/products/mipad6pro.jpg', 100, 1),

-- 智能手表
('Apple Watch Series 9', 13, '智能手表', 1, '苹果', 2999.00, 'Apple Watch Series 9 45mm 午夜色',
 '/images/products/applewatch9.jpg', 80, 1),
('华为 Watch GT 4', 13, '智能手表', 2, '华为', 1999.00, '华为 Watch GT 4 46mm 曜石黑', '/images/products/watchgt4.jpg',
 120, 1),

-- 耳机音响
('AirPods Pro 2', 14, '耳机音响', 1, '苹果', 1899.00, 'AirPods Pro 2代 主动降噪耳机',
 '/images/products/airpodspro2.jpg', 200, 1),
('华为 FreeBuds Pro 3', 14, '耳机音响', 2, '华为', 1499.00, '华为 FreeBuds Pro 3 主动降噪耳机',
 '/images/products/freebudspro3.jpg', 150, 1),

-- 笔记本电脑
('MacBook Pro 14', 21, '笔记本电脑', 1, '苹果', 14999.00, 'MacBook Pro 14英寸 M3 Pro 512GB',
 '/images/products/macbookpro14.jpg', 30, 1),
('华为 MateBook X Pro', 21, '笔记本电脑', 2, '华为', 9999.00, '华为 MateBook X Pro 14英寸 i7 512GB',
 '/images/products/matebookxpro.jpg', 40, 1),
('小米笔记本 Pro 15', 21, '笔记本电脑', 3, '小米', 5999.00, '小米笔记本 Pro 15英寸 i5 512GB',
 '/images/products/milaptop15.jpg', 80, 1),
('联想 ThinkPad X1', 21, '笔记本电脑', 7, '联想', 12999.00, '联想 ThinkPad X1 Carbon i7 512GB',
 '/images/products/thinkpadx1.jpg', 25, 1),
('戴尔 XPS 13', 21, '笔记本电脑', 8, '戴尔', 9999.00, '戴尔 XPS 13 i7 512GB 超轻薄本', '/images/products/dellxps13.jpg',
 35, 1),

-- 台式电脑
('iMac 24', 22, '台式电脑', 1, '苹果', 12999.00, 'iMac 24英寸 M3 256GB 一体机', '/images/products/imac24.jpg', 20, 1),
('华为 MateStation', 22, '台式电脑', 2, '华为', 5999.00, '华为 MateStation B520 i5 512GB',
 '/images/products/matestation.jpg', 30, 1),

-- 办公设备
('打印机 HP LaserJet', 23, '办公设备', 8, '戴尔', 1999.00, 'HP LaserJet Pro M404n 激光打印机',
 '/images/products/hplaserjet.jpg', 50, 1),
('扫描仪 Canon', 23, '办公设备', 7, '联想', 899.00, 'Canon CanoScan Lide 300 扫描仪', '/images/products/canonscan.jpg',
 60, 1),

-- 网络设备
('路由器 TP-Link', 24, '网络设备', 3, '小米', 299.00, 'TP-Link Archer C7 双频路由器', '/images/products/tplink.jpg',
 100, 1),
('交换机 华为', 24, '网络设备', 2, '华为', 1999.00, '华为 S5700-24P-LI-AC 交换机', '/images/products/huaweiswitch.jpg',
 20, 1),

-- 家用电器
('索尼 65英寸 4K电视', 31, '电视', 4, '三星', 5999.00, '索尼 KD-65X85K 65英寸 4K HDR智能电视',
 '/images/products/sonytv65.jpg', 15, 1),
('海信 55英寸 4K电视', 31, '电视', 2, '华为', 3999.00, '海信 55E3F 55英寸 4K超高清智能电视',
 '/images/products/hisensetv55.jpg', 25, 1),
('海尔 对开门冰箱', 32, '冰箱', 3, '小米', 3999.00, '海尔 BCD-470WDPG 470升对开门冰箱',
 '/images/products/haierfridge.jpg', 10, 1),
('美的 滚筒洗衣机', 33, '洗衣机', 5, 'OPPO', 2999.00, '美的 MG100V11D 10公斤滚筒洗衣机',
 '/images/products/midewasher.jpg', 20, 1),
('格力 1.5匹空调', 34, '空调', 6, 'vivo', 2999.00, '格力 KFR-35GW/NhZaD3 1.5匹变频空调', '/images/products/greac.jpg',
 30, 1),

-- 服装鞋帽
('Nike 运动鞋', 43, '运动鞋', 7, '联想', 899.00, 'Nike Air Max 270 运动鞋 42码', '/images/products/nikeshoes.jpg', 100,
 1),
('Adidas 运动鞋', 43, '运动鞋', 8, '戴尔', 799.00, 'Adidas Ultraboost 21 运动鞋 41码',
 '/images/products/adidasshoes.jpg', 80, 1),
('李宁 运动鞋', 43, '运动鞋', 1, '苹果', 599.00, '李宁 云五代 运动鞋 40码', '/images/products/liningshoes.jpg', 120, 1);