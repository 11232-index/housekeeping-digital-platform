CREATE TABLE `updates` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `title` varchar(100) NOT NULL COMMENT '动态标题',
  `picture` varchar(500) DEFAULT NULL COMMENT '图片路径',
  `content` text COMMENT '动态内容',
  `create_user` bigint DEFAULT NULL COMMENT '创建者',
  `update_user` bigint DEFAULT NULL COMMENT '更新者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='动态消息表';
