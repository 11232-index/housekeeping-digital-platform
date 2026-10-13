USE `smart_housekeeping_system`;
CREATE TABLE `hot_picks` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `name` varchar(100) NOT NULL COMMENT '服务名称',
  `picture` varchar(500) NOT NULL COMMENT '服务推荐图片路径',
  `status` int DEFAULT '0' COMMENT '状态 0关闭 1启用',
  `create_user` bigint DEFAULT NULL COMMENT '创建者',
  `update_user` bigint DEFAULT NULL COMMENT '更新者',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务推荐表';
