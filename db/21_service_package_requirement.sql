USE `smart_housekeeping_system`;
CREATE TABLE `service_package_requirement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `service_package_id` bigint NOT NULL COMMENT '服务套餐id',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs DEFAULT NULL COMMENT '要求名称',
  `value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs DEFAULT NULL COMMENT '要求数据list',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs COMMENT='服务套餐项目关系';
