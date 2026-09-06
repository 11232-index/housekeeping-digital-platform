USE `smart_housekeeping_system`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `order_detail`;
-- 订单表
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `number` varchar(50) NOT NULL COMMENT '订单号',
  `status` int DEFAULT NULL COMMENT '订单状态 1待付款 2待接单 3已接单 4服务中 5已完成 6退款中 7已取消',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `address_book_id` bigint DEFAULT NULL COMMENT '地址id',
  `housekeeper_schedule_id` bigint DEFAULT NULL COMMENT '关联housekeeper_schedule表id',
  `schedule_date` date DEFAULT NULL COMMENT '预约日期',
  `schedule_time` varchar(64) DEFAULT NULL COMMENT '预约时段（如：09:00~11:00）',
  `service_duration` varchar(32) DEFAULT NULL COMMENT '服务时长（如：2小时、半天、全天）',
  `order_time` datetime DEFAULT NULL COMMENT '下单时间',
  `pay_time` datetime DEFAULT NULL COMMENT '付款时间',
  `pay_method` int DEFAULT NULL COMMENT '支付方式 1微信支付 2支付宝支付',
  `pay_status` tinyint DEFAULT '0' COMMENT '支付状态 0未支付 1已支付 2退款',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '订单金额',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注信息',
  `phone` varchar(11) DEFAULT NULL COMMENT '手机号（冗余）',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址（冗余）',
  `contact_name` varchar(32) DEFAULT NULL COMMENT '联系人',
  `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `estimated_service_time` datetime DEFAULT NULL COMMENT '预计上门时间',
  `actual_service_time` datetime DEFAULT NULL COMMENT '实际上门时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_number` (`number`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_order_time` (`order_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 订单明细表
CREATE TABLE `order_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '订单id',
  `housekeeper_id` bigint DEFAULT NULL COMMENT '家政员id',
  `service_item_id` bigint DEFAULT NULL COMMENT '服务项目id',
  `service_package_id` bigint DEFAULT NULL COMMENT '服务套餐id',
  `name` varchar(32) DEFAULT NULL COMMENT '名称（家政员姓名/服务名/套餐名）',
  `image` varchar(255) DEFAULT NULL COMMENT '图片',
  `requirement` varchar(255) DEFAULT NULL COMMENT '服务要求（如：需自带工具、周末服务）',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_housekeeper_id` (`housekeeper_id`),
  KEY `idx_service_item_id` (`service_item_id`),
  KEY `idx_service_package_id` (`service_package_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';
