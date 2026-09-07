CREATE TABLE `service_package_requirement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `service_package_id` bigint NOT NULL COMMENT '服务套餐id',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '要求名称',
  `value` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '要求数据list',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='服务套餐项目关系';
