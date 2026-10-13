USE `smart_housekeeping_system`;

-- ============================================================
-- 秒杀改造（Redis 预扣库存）配套 DDL
--
-- 背景：把秒杀券的库存判断从 MySQL 搬到 Redis（见 lua/seckill.lua）。
--      随之而来一个语义变化，必须先记住：
--
--        tb_seckill_voucher.stock
--          改造前 = 剩余库存（每次领取都 DECREASE）
--          改造后 = 总发放额度（领取不再递减；剩余可领量存在 Redis 的 seckill:stock:{voucher_id}）
--
--      本文件只做 DDL，可以安全重复执行。
--      存量数据的换算在 34_migrate_seckill_stock_to_quota.sql —— 那个文件只能执行一次！
-- ============================================================

-- 1) 按券统计"已发份数"用的索引
--    对账任务和懒预热都要执行 select count(*) from tb_user_voucher where voucher_id = ?
--    而这张表现有的索引都以 user_id 打头（uk_user_voucher / idx_user_status），
--    voucher_id 单列查询用不上，会全表扫描。
ALTER TABLE `tb_user_voucher` ADD INDEX `idx_voucher` (`voucher_id`);

-- 2) 把新语义写进列注释，避免后人误以为它还是"剩余库存"
ALTER TABLE `tb_seckill_voucher`
    MODIFY COLUMN `stock` int NOT NULL
    COMMENT '总发放额度（改造后不再随领取递减；剩余可领数量见 Redis 的 seckill:stock:{voucher_id}）';
