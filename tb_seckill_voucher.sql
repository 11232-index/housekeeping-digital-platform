USE `smart_housekeeping_system`;

-- 秒杀优惠券表，与 tb_voucher（优惠券表）是一对一关系
DROP TABLE IF EXISTS `tb_seckill_voucher`;
CREATE TABLE `tb_seckill_voucher` (
    `voucher_id` bigint(20) unsigned NOT NULL COMMENT '关联的优惠券的id',
    `stock` int(8) unsigned NOT NULL COMMENT '库存',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `begin_time` timestamp NULL DEFAULT NULL COMMENT '生效时间',
    `end_time` timestamp NULL DEFAULT NULL COMMENT '失效时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`voucher_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀优惠券表，与优惠券是一对一关系';
