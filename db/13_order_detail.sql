USE `smart_housekeeping_system`;
CREATE TABLE `order_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '订单id',
  `housekeeper_id` bigint DEFAULT NULL COMMENT '家政员id',
  `service_item_id` bigint DEFAULT NULL COMMENT '服务项目id',
  `service_package_id` bigint DEFAULT NULL COMMENT '服务套餐id',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs DEFAULT NULL COMMENT '名称（家政员姓名/服务名/套餐名）',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs DEFAULT NULL COMMENT '图片',
  `requirement` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs DEFAULT NULL COMMENT '服务要求（如：需自带工具、周末服务）',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_housekeeper_id` (`housekeeper_id`),
  KEY `idx_service_item_id` (`service_item_id`),
  KEY `idx_service_package_id` (`service_package_id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs COMMENT='订单明细表';
