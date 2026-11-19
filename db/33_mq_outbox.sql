USE `smart_housekeeping_system`;

CREATE TABLE `mq_outbox` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `event_id` varchar(64) NOT NULL COMMENT '事件唯一ID（消费端幂等用）',
  `event_type` varchar(64) NOT NULL COMMENT '事件类型/路由键，如 order.created',
  `routing_key` varchar(64) NOT NULL COMMENT '发送到 order.events 时使用的 routing key',
  `aggregate_type` varchar(32) NOT NULL DEFAULT 'order' COMMENT '聚合类型',
  `aggregate_id` bigint DEFAULT NULL COMMENT '订单id等聚合根id',
  `payload` text NOT NULL COMMENT '消息体 JSON',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待发送 1已发送 2发送失败',
  `retry_count` int NOT NULL DEFAULT '0' COMMENT '重试次数',
  `next_retry_time` datetime DEFAULT NULL COMMENT '下次重试时间',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '最近一次失败原因',
  `sent_time` datetime DEFAULT NULL COMMENT '发送成功时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_id` (`event_id`),
  KEY `idx_status_next_retry` (`status`, `next_retry_time`),
  KEY `idx_aggregate` (`aggregate_type`, `aggregate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs COMMENT='RabbitMQ 本地消息表（事务内写、定时任务发送）';
