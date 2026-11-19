USE `smart_housekeeping_system`;
CREATE TABLE `order_notify` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type` varchar(20) NOT NULL COMMENT '消息类型：newOrder 来单提醒；urge 客户催单',
  `title` varchar(100) DEFAULT NULL COMMENT '提示标题',
  `content` varchar(500) DEFAULT NULL COMMENT '提示/播报内容',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单id',
  `order_number` varchar(50) DEFAULT NULL COMMENT '订单号（冗余）',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '订单金额',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs COMMENT='订单提醒消息表';
