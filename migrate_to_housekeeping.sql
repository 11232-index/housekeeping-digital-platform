-- ============================================================
-- 智能点餐系统 → 智能家政服务  数据库迁移脚本
-- 针对现有 smart_ordering_system 数据库进行原地改造
-- ============================================================

USE `smart_housekeeping_system`;

-- ============================================================
-- 第一步：重命名数据库（需要单独执行，不能在USE后执行）
-- 请在MySQL客户端执行:
--   ALTER DATABASE `smart_ordering_system` MODIFY NAME = `smart_housekeeping_system`;
--   或手动创建新库后导入，再用此脚本改造
-- ============================================================

-- ============================================================
-- 第二步：重命名表
-- ============================================================
RENAME TABLE `dish` TO `service_item`;
RENAME TABLE `dish_flavor` TO `service_requirement`;
RENAME TABLE `setmeal` TO `service_package`;
RENAME TABLE `setmeal_dish` TO `service_package_item`;
RENAME TABLE `orders` TO `service_order`;
RENAME TABLE `order_detail` TO `service_order_detail`;
RENAME TABLE `shopping_cart` TO `booking_list`;

-- ============================================================
-- 第三步：修改列名
-- ============================================================

-- service_requirement: dish_id → service_item_id
ALTER TABLE `service_requirement` CHANGE `dish_id` `service_item_id` bigint NOT NULL COMMENT '服务项目id';

-- service_package_item: setmeal_id → service_package_id, dish_id → service_item_id
ALTER TABLE service_package_requirement CHANGE `setmeal_id` `service_package_id` bigint DEFAULT NULL COMMENT '服务套餐id';
ALTER TABLE service_package_requirement CHANGE `dish_id` `service_item_id` bigint DEFAULT NULL COMMENT '服务项目id';
ALTER TABLE service_package_requirement CHANGE `name` `name` varchar(32) DEFAULT NULL COMMENT '服务项目名称（冗余字段）';
ALTER TABLE service_package_requirement CHANGE `price` `price` decimal(10,2) DEFAULT NULL COMMENT '服务项目单价（冗余字段）';
ALTER TABLE service_package_requirement CHANGE `copies` `copies` int DEFAULT NULL COMMENT '服务次数';

-- service_order_detail: dish_id→service_item_id, setmeal_id→service_package_id, dish_flavor→requirement
ALTER TABLE `service_order_detail` CHANGE `dish_id` `service_item_id` bigint DEFAULT NULL COMMENT '服务项目id';
ALTER TABLE `service_order_detail` CHANGE `setmeal_id` `service_package_id` bigint DEFAULT NULL COMMENT '服务套餐id';
ALTER TABLE `service_order_detail` CHANGE `dish_flavor` `requirement` varchar(50) DEFAULT NULL COMMENT '服务要求';

-- service_order: 重命名配送相关列
ALTER TABLE `service_order` CHANGE `estimated_delivery_time` `estimated_service_time` datetime DEFAULT NULL COMMENT '预计上门时间';
ALTER TABLE `service_order` CHANGE `delivery_status` `service_time_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '上门时间类型  1立即上门  0选择具体时间';
ALTER TABLE `service_order` CHANGE `delivery_time` `service_time` datetime DEFAULT NULL COMMENT '实际上门时间';
ALTER TABLE `service_order` CHANGE `pack_amount` `material_amount` int DEFAULT NULL COMMENT '材料费';
ALTER TABLE `service_order` CHANGE `tableware_number` `tool_number` int DEFAULT NULL COMMENT '工具数量';
ALTER TABLE `service_order` CHANGE `tableware_status` `tool_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '工具状态  1按需提供  0选择具体数量';

-- booking_list: dish_id→service_item_id, setmeal_id→service_package_id, dish_flavor→requirement
ALTER TABLE `booking_list` CHANGE `dish_id` `service_item_id` bigint DEFAULT NULL COMMENT '服务项目id';
ALTER TABLE `booking_list` CHANGE `setmeal_id` `service_package_id` bigint DEFAULT NULL COMMENT '服务套餐id';
ALTER TABLE `booking_list` CHANGE `dish_flavor` `requirement` varchar(50) DEFAULT NULL COMMENT '服务要求';

-- ============================================================
-- 第四步：更新表注释
-- ============================================================
ALTER TABLE `category` COMMENT = '家政服务分类';
ALTER TABLE `service_item` COMMENT = '服务项目';
ALTER TABLE `service_requirement` COMMENT = '服务要求关系表';
ALTER TABLE `service_package` COMMENT = '服务套餐';
ALTER TABLE service_package_requirement COMMENT = '服务套餐项目关系';
ALTER TABLE `service_order` COMMENT = '服务订单表';
ALTER TABLE `service_order_detail` COMMENT = '服务订单明细表';
ALTER TABLE `booking_list` COMMENT = '预约清单';
ALTER TABLE `address_book` COMMENT = '地址簿';
ALTER TABLE `employee` COMMENT = '家政人员信息';
ALTER TABLE `user` COMMENT = '客户信息';

-- ============================================================
-- 第五步：更新列注释
-- ============================================================

-- category表
ALTER TABLE `category` MODIFY COLUMN `type` int DEFAULT NULL COMMENT '类型   1 服务分类 2 服务套餐分类';

-- service_item表 (原dish)
ALTER TABLE `service_item` MODIFY COLUMN `name` varchar(32) NOT NULL COMMENT '服务项目名称';
ALTER TABLE `service_item` MODIFY COLUMN `category_id` bigint NOT NULL COMMENT '服务分类id';
ALTER TABLE `service_item` MODIFY COLUMN `price` decimal(10,2) DEFAULT NULL COMMENT '服务价格';
ALTER TABLE `service_item` MODIFY COLUMN `status` int DEFAULT '1' COMMENT '0 休息中 1 可预约';

-- service_package表 (原setmeal)
ALTER TABLE `service_package` MODIFY COLUMN `category_id` bigint NOT NULL COMMENT '服务分类id';
ALTER TABLE `service_package` MODIFY COLUMN `name` varchar(32) NOT NULL COMMENT '服务套餐名称';
ALTER TABLE `service_package` MODIFY COLUMN `price` decimal(10,2) NOT NULL COMMENT '套餐价格';
ALTER TABLE `service_package` MODIFY COLUMN `status` int DEFAULT '1' COMMENT '售卖状态 0:休息中 1:可预约';

-- service_order表 (原orders)
ALTER TABLE `service_order` MODIFY COLUMN `number` varchar(50) DEFAULT NULL COMMENT '服务单号';
ALTER TABLE `service_order` MODIFY COLUMN `status` int NOT NULL DEFAULT '1' COMMENT '服务状态 1待付款 2待接单 3已接单 4服务中 5已完成 6已取消 7退款';
ALTER TABLE `service_order` MODIFY COLUMN `user_id` bigint NOT NULL COMMENT '预约客户id';
ALTER TABLE `service_order` MODIFY COLUMN `order_time` datetime NOT NULL COMMENT '预约时间';
ALTER TABLE `service_order` MODIFY COLUMN `checkout_time` datetime DEFAULT NULL COMMENT '完成时间';
ALTER TABLE `service_order` MODIFY COLUMN `address` varchar(255) DEFAULT NULL COMMENT '服务地址';
ALTER TABLE `service_order` MODIFY COLUMN `user_name` varchar(32) DEFAULT NULL COMMENT '客户名称';
ALTER TABLE `service_order` MODIFY COLUMN `consignee` varchar(32) DEFAULT NULL COMMENT '联系人';

-- address_book表
ALTER TABLE `address_book` MODIFY COLUMN `user_id` bigint NOT NULL COMMENT '客户id';
ALTER TABLE `address_book` MODIFY COLUMN `consignee` varchar(50) DEFAULT NULL COMMENT '联系人';

-- booking_list表 (原shopping_cart)
ALTER TABLE `booking_list` MODIFY COLUMN `name` varchar(32) DEFAULT NULL COMMENT '服务名称';
ALTER TABLE `booking_list` MODIFY COLUMN `user_id` bigint NOT NULL COMMENT '客户id';

-- user表
ALTER TABLE `user` COMMENT = '客户信息';

-- ============================================================
-- 第六步：清空旧种子数据，插入家政服务种子数据
-- ============================================================

-- 6.1 清空旧数据（保留employee管理员账号）
DELETE FROM `service_requirement`;
DELETE FROM service_package_requirement;
DELETE FROM `service_order_detail`;
DELETE FROM `service_order`;
DELETE FROM `booking_list`;
DELETE FROM `service_package`;
DELETE FROM `service_item`;
DELETE FROM `category`;

-- 6.2 家政服务分类数据
INSERT INTO `category` VALUES (11,1,'日常保洁',10,1,'2022-06-09 22:09:18','2022-06-09 22:09:18',1,1);
INSERT INTO `category` VALUES (12,1,'家电清洗',9,1,'2022-06-09 22:09:32','2022-06-09 22:18:53',1,1);
INSERT INTO `category` VALUES (13,2,'深度保洁套餐',12,1,'2022-06-09 22:11:38','2022-06-10 11:04:40',1,1);
INSERT INTO `category` VALUES (15,2,'家庭综合套餐',13,1,'2022-06-09 22:14:10','2022-06-10 11:04:48',1,1);
INSERT INTO `category` VALUES (16,1,'月嫂服务',4,1,'2022-06-09 22:15:37','2022-08-31 14:27:25',1,1);
INSERT INTO `category` VALUES (17,1,'育儿嫂服务',5,1,'2022-06-09 22:16:14','2022-08-31 14:39:44',1,1);
INSERT INTO `category` VALUES (18,1,'老人陪护',6,1,'2022-06-09 22:17:42','2022-06-09 22:17:42',1,1);
INSERT INTO `category` VALUES (19,1,'维修服务',7,1,'2022-06-09 22:18:12','2022-06-09 22:18:28',1,1);
INSERT INTO `category` VALUES (20,1,'搬家服务',8,1,'2022-06-09 22:22:29','2022-06-09 22:23:45',1,1);
INSERT INTO `category` VALUES (21,1,'空气治理',11,1,'2022-06-10 10:51:47','2022-06-10 10:51:47',1,1);

-- 6.3 家政服务项目数据（对应原菜品）
INSERT INTO `service_item` VALUES (46,'两室一厅日常保洁',11,120.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/41bfcacf-7ad4-4927-8b26-df366553a94c.png','含客厅、卧室、厨房、卫生间清洁',1,'2022-06-09 22:40:47','2022-06-09 22:40:47',1,1);
INSERT INTO `service_item` VALUES (47,'三室一厅日常保洁',11,160.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/4451d4be-89a2-4939-9c69-3a87151cb979.png','含全屋地面、家具表面、厨卫清洁',1,'2022-06-10 09:18:49','2022-06-10 09:18:49',1,1);
INSERT INTO `service_item` VALUES (48,'开荒保洁',11,500.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/bf8cbfc1-04d2-40e8-9826-061ee41ab87c.png','新房装修后全面清洁，含门窗、地面、厨卫',1,'2022-06-10 09:22:54','2022-06-10 09:22:54',1,1);
INSERT INTO `service_item` VALUES (49,'空调深度清洗',12,150.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/76752350-2121-44d2-b477-10791c23a8ec.png','挂机深度清洗，含过滤网、蒸发器、风轮',1,'2022-06-10 09:30:17','2022-06-10 09:30:17',1,1);
INSERT INTO `service_item` VALUES (50,'洗衣机深度清洗',12,180.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/475cc599-8661-4899-8f9e-121dd8ef7d02.png','滚筒/波轮洗衣机深度拆洗除垢',1,'2022-06-10 09:34:28','2022-06-10 09:34:28',1,1);
INSERT INTO `service_item` VALUES (51,'油烟机全拆洗',12,200.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/4a9cefba-6a74-467e-9fde-6e687ea725d7.png','中式/欧式油烟机全拆洗，含油网、风轮',1,'2022-06-10 09:40:51','2022-06-10 09:40:51',1,1);
INSERT INTO `service_item` VALUES (52,'冰箱深度清洗',12,160.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/5260ff39-986c-4a97-8850-2ec8c7583efc.png','冰箱内部除霜除味、密封条清洗消毒',1,'2022-06-10 09:46:02','2022-06-10 09:46:02',1,1);
INSERT INTO `service_item` VALUES (53,'热水器除垢清洗',12,180.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/a6953d5a-4c18-4b30-9319-4926ee77261f.png','电热水器除垢清洗，含加热棒、内胆',1,'2022-06-10 09:48:37','2022-06-10 09:48:37',1,1);
INSERT INTO `service_item` VALUES (54,'下水道疏通',19,100.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/3613d38e-5614-41c2-90ed-ff175bf50716.png','厨房/卫生间下水道疏通',1,'2022-06-10 09:51:46','2022-06-10 09:51:46',1,1);
INSERT INTO `service_item` VALUES (55,'电路检修',19,150.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/4879ed66-3860-4b28-ba14-306ac025fdec.png','开关插座更换、线路检修',1,'2022-06-10 09:53:37','2022-06-10 09:53:37',1,1);
INSERT INTO `service_item` VALUES (56,'马桶维修疏通',19,120.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/e9ec4ba4-4b22-4fc8-9be0-4946e6aeb937.png','马桶堵塞疏通、配件更换',1,'2022-06-10 09:55:44','2022-06-10 09:55:44',1,1);
INSERT INTO `service_item` VALUES (57,'墙面修补刷漆',19,200.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/22f59feb-0d44-430e-a6cd-6a49f27453ca.png','墙面裂缝修补、局部刷漆',1,'2022-06-10 09:58:35','2022-06-10 09:58:35',1,1);
INSERT INTO `service_item` VALUES (58,'金牌月嫂',16,12800.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/c18b5c67-3b71-466c-a75a-e63c6449f21c.png','持证月嫂，26天住家服务，含母婴护理',1,'2022-06-10 10:12:28','2022-06-10 10:12:28',1,1);
INSERT INTO `service_item` VALUES (59,'高级月嫂',16,9800.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/a80a4b8c-c93e-4f43-ac8a-856b0d5cc451.png','3年以上经验月嫂，26天住家服务',1,'2022-06-10 10:24:03','2022-06-10 10:24:03',1,1);
INSERT INTO `service_item` VALUES (60,'普通月嫂',16,7800.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/6080b118-e30a-4577-aab4-45042e3f88be.png','持证月嫂，26天住家基础护理',1,'2022-06-10 10:26:03','2022-06-10 10:26:03',1,1);
INSERT INTO `service_item` VALUES (61,'白班月嫂',16,6500.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/13da832f-ef2c-484d-8370-5934a1045a06.png','白天8小时月嫂服务，26天',1,'2022-06-10 10:28:54','2022-06-10 10:28:54',1,1);
INSERT INTO `service_item` VALUES (62,'高级育儿嫂',17,7500.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/7694a5d8-7938-4e9d-8b9e-2075983a2e38.png','持证育儿嫂，住家婴幼儿照护',1,'2022-06-10 10:33:05','2022-06-10 10:33:05',1,1);
INSERT INTO `service_item` VALUES (63,'普通育儿嫂',17,5500.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/f5ac8455-4793-450c-97ba-173795c34626.png','1年以上经验育儿嫂，住家照护',1,'2022-06-10 10:35:40','2022-06-10 10:35:40',1,1);
INSERT INTO `service_item` VALUES (64,'白班育儿嫂',17,4500.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/7a55b845-1f2b-41fa-9486-76d187ee9ee1.png','白天8小时婴幼儿照护服务',1,'2022-06-10 10:37:52','2022-06-10 10:37:52',1,1);
INSERT INTO `service_item` VALUES (65,'老人全天陪护',18,6000.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/b544d3ba-a1ae-4d20-a860-81cb5dec9e03.png','24小时住家老人陪护，含生活照料',1,'2022-06-10 10:41:08','2022-06-10 10:41:08',1,1);
INSERT INTO `service_item` VALUES (66,'老人日间陪护',18,4000.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/a101a1e9-8f8b-47b2-afa4-1abd47ea0a87.png','白天8小时老人陪护，含餐食照料',1,'2022-06-10 10:42:42','2022-06-10 10:42:42',1,1);
INSERT INTO `service_item` VALUES (67,'半自理老人陪护',18,5000.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/8cfcc576-4b66-4a09-ac68-ad5b273c2590.png','针对半自理老人的专业护理服务',1,'2022-06-10 10:43:56','2022-06-10 10:43:56',1,1);
INSERT INTO `service_item` VALUES (68,'甲醛检测',21,300.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/c09a0ee8-9d19-428d-81b9-746221824113.png','专业仪器全屋甲醛检测',1,'2022-06-10 10:54:25','2022-06-10 10:54:25',1,1);
INSERT INTO `service_item` VALUES (69,'甲醛治理',21,2500.00,'https://sky-change-me-secret.oss-cn-beijing.aliyuncs.com/16d0a3d6-2253-4cfc-9b49-bf7bd9eb2ad2.png','全屋甲醛治理，含施工和复测',1,'2022-06-10 10:55:02','2022-06-10 10:55:02',1,1);

-- 6.4 服务要求数据（对应原口味）
INSERT INTO `service_requirement` VALUES (40,10,'服务频次','[\"单次\",\"每周一次\",\"每周两次\",\"每月一次\"]');
INSERT INTO `service_requirement` VALUES (41,7,'特殊要求','[\"无\",\"需自带工具\",\"需环保清洁剂\",\"需周末服务\"]');
INSERT INTO `service_requirement` VALUES (42,7,'服务时段','[\"上午\",\"下午\",\"全天\",\"灵活安排\"]');
INSERT INTO `service_requirement` VALUES (45,6,'特殊要求','[\"无\",\"需自带工具\",\"需环保清洁剂\",\"需周末服务\"]');
INSERT INTO `service_requirement` VALUES (46,6,'服务时段','[\"上午\",\"下午\",\"全天\",\"灵活安排\"]');
INSERT INTO `service_requirement` VALUES (47,5,'服务时段','[\"上午\",\"下午\",\"全天\",\"灵活安排\"]');
INSERT INTO `service_requirement` VALUES (48,5,'额外服务','[\"无需\",\"加清洁剂\",\"加消毒\",\"加除螨\"]');
INSERT INTO `service_requirement` VALUES (49,2,'额外服务','[\"无需\",\"加清洁剂\",\"加消毒\",\"加除螨\"]');
INSERT INTO `service_requirement` VALUES (50,4,'额外服务','[\"无需\",\"加清洁剂\",\"加消毒\",\"加除螨\"]');
INSERT INTO `service_requirement` VALUES (51,3,'额外服务','[\"无需\",\"加清洁剂\",\"加消毒\",\"加除螨\"]');
INSERT INTO `service_requirement` VALUES (52,3,'特殊要求','[\"无\",\"需自带工具\",\"需环保清洁剂\",\"需周末服务\"]');
INSERT INTO `service_requirement` VALUES (86,52,'特殊要求','[\"无\",\"需自带工具\",\"需环保清洁剂\",\"需周末服务\"]');
INSERT INTO `service_requirement` VALUES (87,52,'服务时段','[\"上午\",\"下午\",\"全天\",\"灵活安排\"]');
INSERT INTO `service_requirement` VALUES (88,51,'特殊要求','[\"无\",\"需自带工具\",\"需环保清洁剂\",\"需周末服务\"]');
INSERT INTO `service_requirement` VALUES (89,51,'服务时段','[\"上午\",\"下午\",\"全天\",\"灵活安排\"]');
INSERT INTO `service_requirement` VALUES (92,53,'特殊要求','[\"无\",\"需自带工具\",\"需环保清洁剂\",\"需周末服务\"]');
INSERT INTO `service_requirement` VALUES (93,53,'服务时段','[\"上午\",\"下午\",\"全天\",\"灵活安排\"]');
INSERT INTO `service_requirement` VALUES (94,54,'特殊要求','[\"无\",\"需自带工具\",\"需周末服务\"]');
INSERT INTO `service_requirement` VALUES (95,56,'特殊要求','[\"无\",\"需自带工具\",\"需环保清洁剂\",\"需周末服务\"]');
INSERT INTO `service_requirement` VALUES (96,57,'特殊要求','[\"无\",\"需自带工具\",\"需环保清洁剂\",\"需周末服务\"]');
INSERT INTO `service_requirement` VALUES (97,60,'特殊要求','[\"无\",\"需自带工具\",\"需环保清洁剂\",\"需周末服务\"]');
INSERT INTO `service_requirement` VALUES (101,66,'服务时段','[\"上午\",\"下午\",\"全天\",\"灵活安排\"]');
INSERT INTO `service_requirement` VALUES (102,67,'服务时段','[\"上午\",\"下午\",\"全天\",\"灵活安排\"]');
INSERT INTO `service_requirement` VALUES (103,65,'服务时段','[\"上午\",\"下午\",\"全天\",\"灵活安排\"]');

-- ============================================================
-- 完成
-- ============================================================
SELECT '============================================' AS '';
SELECT '  数据库迁移完成！' AS '';
SELECT '  旧表名 → 新表名映射：' AS '';
SELECT '    dish            → service_item' AS '';
SELECT '    dish_flavor     → service_requirement' AS '';
SELECT '    setmeal         → service_package' AS '';
SELECT '    setmeal_dish    → service_package_item' AS '';
SELECT '    orders          → service_order' AS '';
SELECT '    order_detail    → service_order_detail' AS '';
SELECT '    shopping_cart   → booking_list' AS '';
SELECT '============================================' AS '';
SELECT '  提示：请手动将数据库重命名：' AS '';
SELECT '  ALTER DATABASE `smart_ordering_system` MODIFY NAME = `smart_housekeeping_system`;' AS '';
SELECT '============================================' AS '';
