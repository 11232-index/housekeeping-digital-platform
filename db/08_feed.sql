CREATE TABLE `feed` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `updates_id` bigint DEFAULT NULL COMMENT '关联updates表id',
  `service_intro_id` bigint DEFAULT NULL COMMENT '关联service_intro表id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_updates` (`updates_id`),
  KEY `idx_service_intro` (`service_intro_id`),
  KEY `idx_time` (`create_time` DESC)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资讯动态统一表（联表）';
