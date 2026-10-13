USE `smart_housekeeping_system`;
CREATE TABLE `about` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `title` varchar(100) NOT NULL COMMENT '题目',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `content` text COMMENT '具体内容',
  `create_user` bigint DEFAULT NULL COMMENT '创建者',
  `update_user` bigint DEFAULT NULL COMMENT '更新者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='关于我们表';
