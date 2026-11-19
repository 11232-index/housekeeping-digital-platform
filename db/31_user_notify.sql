USE `smart_housekeeping_system`;
CREATE TABLE `user_notify` (
  `id`           bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`      bigint NOT NULL COMMENT '接收用户id',
  `type`         varchar(20) NOT NULL COMMENT '消息类型：accepted接单 rejected拒单 cannotServe无法服务 inService开始服务 completed已完成 cancelled已取消 refunded退款完成 timeout超时',
  `title`        varchar(100) DEFAULT NULL COMMENT '提示标题',
  `content`      varchar(500) DEFAULT NULL COMMENT '提示内容',
  `order_id`     bigint DEFAULT NULL COMMENT '关联订单id',
  `order_number` varchar(50) DEFAULT NULL COMMENT '订单号（冗余，便于直接展示）',
  `read_flag`    tinyint NOT NULL DEFAULT '0' COMMENT '是否已读 0未读 1已读',
  `create_time`  datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `read_flag`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs COMMENT='用户订单消息表';
