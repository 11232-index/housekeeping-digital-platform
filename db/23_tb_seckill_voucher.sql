USE `smart_housekeeping_system`;
CREATE TABLE `tb_seckill_voucher` (
  `voucher_id` bigint unsigned NOT NULL COMMENT '关联的优惠券的id',
  `stock` int unsigned NOT NULL COMMENT '总发放额度（改造后不再随领取递减；剩余可领数量见 Redis 的 seckill:stock:{voucher_id}）',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `begin_time` timestamp NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '失效时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`voucher_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs COMMENT='秒杀优惠券表，与优惠券是一对一关系';
