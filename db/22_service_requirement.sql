USE `smart_housekeeping_system`;
CREATE TABLE `service_requirement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `service_item_id` bigint NOT NULL COMMENT '服务项目id',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '要求名称',
  `value` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '要求数据list',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=161 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='服务要求关系表';
