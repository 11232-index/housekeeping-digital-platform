USE `smart_housekeeping_system`;
CREATE TABLE `housekeeper` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `employee_id` bigint NOT NULL COMMENT '关联员工表id',
  `name` varchar(50) NOT NULL COMMENT '家政员名字',
  `picture` varchar(500) DEFAULT NULL COMMENT '图片路径',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '单价',
  `rating` decimal(2,1) DEFAULT '0.0' COMMENT '评分',
  `rank` int DEFAULT '0' COMMENT '身价 0无牌 1金牌 2银牌 3铜牌',
  `carousel` text COMMENT '轮播图',
  `review` text COMMENT '综合评价',
  `create_user` bigint DEFAULT NULL COMMENT '创建者',
  `update_user` bigint DEFAULT NULL COMMENT '更新者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_employee_id` (`employee_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='家政员表';
