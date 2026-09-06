USE `smart_housekeeping_system`;

-- 优惠券（代金券）表
-- 说明：由原 tb_voucher 改造，将「商铺id」改为「服务id」，并新增「服务套餐id」，
--      分别关联本项目中的 service_item（服务项目表）与 service_package（服务套餐表）。
DROP TABLE IF EXISTS `tb_voucher`;
CREATE TABLE `tb_voucher` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `service_id` bigint(20) unsigned DEFAULT NULL COMMENT '服务id（关联 service_item 表）',
    `service_package_id` bigint(20) unsigned DEFAULT NULL COMMENT '服务套餐id（关联 service_package 表）',
    `title` varchar(255) NOT NULL COMMENT '代金券标题',
    `sub_title` varchar(255) DEFAULT NULL COMMENT '副标题',
    `rules` varchar(1024) DEFAULT NULL COMMENT '使用规则',
    `pay_value` bigint(10) unsigned NOT NULL COMMENT '支付金额，单位是分。例如200代表2元',
    `actual_value` bigint(10) NOT NULL COMMENT '抵扣金额，单位是分。例如200代表2元',
    `type` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '0,普通券；1,秒杀券',
    `status` tinyint(1) unsigned NOT NULL DEFAULT '1' COMMENT '1,上架；2,下架；3,过期',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4;

