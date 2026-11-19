USE `smart_housekeeping_system`;

-- ============================================================
-- 用户优惠券（领取与使用记录）
-- 增量 DDL，仅执行一次（MySQL 8 的 ADD COLUMN 不支持 IF NOT EXISTS）
--
-- 背景：原来"谁领了哪张券"只记在 Redis 的 Set 里（key=claim:用户id），
--       Redis 一重启记录就没了、也没法查"某个用户有哪些券"。
--       这里落一张表作为可靠来源，Redis 那套保留做快速拦截。
-- ============================================================

-- 1) 用户优惠券表
CREATE TABLE IF NOT EXISTS `tb_user_voucher` (
  `id`          bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     bigint NOT NULL COMMENT '用户id',
  `voucher_id`  bigint NOT NULL COMMENT '优惠券id（关联 tb_voucher）',
  `status`      tinyint NOT NULL DEFAULT '0' COMMENT '0未使用 1已使用',
  `order_id`    bigint DEFAULT NULL COMMENT '使用该券的订单id（用掉后回填）',
  `claim_time`  datetime DEFAULT NULL COMMENT '领取时间',
  `use_time`    datetime DEFAULT NULL COMMENT '使用时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  -- 一人一单：数据库层面兜底，不依赖 Redis 也不会被重复领取
  UNIQUE KEY `uk_user_voucher` (`user_id`, `voucher_id`),
  -- "我的优惠券"按 用户 + 状态 查
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs COMMENT='用户优惠券（领取与使用记录）';

-- 2) 订单表记录用券情况（快照，订单详情直接展示，不用再回查券表）
--    没有用券的订单，这几个字段都是 NULL
ALTER TABLE `orders`
  ADD COLUMN `voucher_id` bigint DEFAULT NULL COMMENT '使用的优惠券id（未使用券为NULL）' AFTER `amount`,
  ADD COLUMN `voucher_title` varchar(255) DEFAULT NULL COMMENT '优惠券标题（快照，便于订单直接展示）' AFTER `voucher_id`,
  ADD COLUMN `voucher_discount` int DEFAULT NULL COMMENT '折扣率快照（80=八折）' AFTER `voucher_title`,
  ADD COLUMN `discount_amount` decimal(10,2) DEFAULT NULL COMMENT '优惠金额（原价 - 实付）' AFTER `voucher_discount`;
