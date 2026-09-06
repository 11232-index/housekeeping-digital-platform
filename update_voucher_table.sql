USE `smart_housekeeping_system`;

-- 优惠券表结构调整：
-- 1. 去掉服务id/服务套餐id（优惠券对所有服务/套餐订单通用）
-- 2. 新增订单id（关联 orders 表），支付时选择使用券后回填
-- 3. status 只保留 1上架/2下架，过期由截止时间自动判断（不再手动设置）
-- 注意：以上为原地迁移脚本，保留现有数据；如需全新表可执行 tb_voucher.sql

ALTER TABLE `tb_voucher` DROP COLUMN `service_id`;
ALTER TABLE `tb_voucher` DROP COLUMN `service_package_id`;
ALTER TABLE `tb_voucher` ADD COLUMN `order_id` bigint(20) DEFAULT NULL COMMENT '使用该优惠券的订单id（关联 orders 表），支付时使用券后回填' AFTER `id`;
ALTER TABLE `tb_voucher` MODIFY COLUMN `pay_value` bigint(10) unsigned NOT NULL DEFAULT '0' COMMENT '原价，单位是分';
ALTER TABLE `tb_voucher` MODIFY COLUMN `actual_value` bigint(10) NOT NULL COMMENT '折扣率（八折=80，八五折=85）';
ALTER TABLE `tb_voucher` MODIFY COLUMN `status` tinyint(1) unsigned NOT NULL DEFAULT '1' COMMENT '1,上架；2,下架';
