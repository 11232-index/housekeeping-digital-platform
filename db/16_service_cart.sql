USE `smart_housekeeping_system`;
CREATE TABLE `service_cart` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '服务名称',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '服务图片路径',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `service_item_id` bigint DEFAULT NULL COMMENT '服务项目id',
  `service_package_id` bigint DEFAULT NULL COMMENT '服务套餐id',
  `requirement` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '服务要求',
  `amount` decimal(10,2) NOT NULL COMMENT '服务单价',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=88 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='购物车表';
