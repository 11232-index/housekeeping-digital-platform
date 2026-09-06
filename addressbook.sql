
USE `smart_housekeeping_system`;

-- =============================================
-- 地址簿表
-- =============================================
DROP TABLE IF EXISTS `address_book`;
CREATE TABLE `address_book` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `consignee` varchar(50) DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `sex` varchar(2) NOT NULL COMMENT '性别 0 女 1 男',
  `province_code` varchar(12) DEFAULT NULL COMMENT '省级区划编号',
  `province_name` varchar(32) DEFAULT NULL COMMENT '省级名称',
  `city_code` varchar(12) DEFAULT NULL COMMENT '市级区划编号',
  `city_name` varchar(32) DEFAULT NULL COMMENT '市级名称',
  `district_code` varchar(12) DEFAULT NULL COMMENT '区级区划编号',
  `district_name` varchar(32) DEFAULT NULL COMMENT '区级名称',
  `detail` varchar(200) NOT NULL COMMENT '详细地址',
  `label` varchar(50) DEFAULT NULL COMMENT '标签（如：家、公司、学校）',
  `is_default` int DEFAULT 0 COMMENT '是否默认 0否 1是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='地址簿表';
