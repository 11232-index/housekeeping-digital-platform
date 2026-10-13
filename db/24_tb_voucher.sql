USE `smart_housekeeping_system`;
CREATE TABLE `tb_voucher` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint DEFAULT NULL COMMENT '使用该优惠券的订单id（关联 orders 表），支付时使用券后回填',
  `title` varchar(255) NOT NULL COMMENT '代金券标题',
  `sub_title` varchar(255) DEFAULT NULL COMMENT '副标题',
  `rules` varchar(1024) DEFAULT NULL COMMENT '使用规则',
  `pay_value` bigint unsigned NOT NULL DEFAULT '0' COMMENT '原价，单位是分',
  `actual_value` bigint NOT NULL COMMENT '折扣率（八折=80，八五折=85）',
  `type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '0,普通券；1,秒杀券',
  `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '1,上架；2,下架',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=354540618334076929 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
