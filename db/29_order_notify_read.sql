USE `smart_housekeeping_system`;
CREATE TABLE `order_notify_read` (
  `employee_id` bigint NOT NULL COMMENT '员工id',
  `notify_id` bigint NOT NULL COMMENT '消息id',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`employee_id`, `notify_id`),
  KEY `idx_notify_id` (`notify_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工已读消息关系表';
