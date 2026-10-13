USE `smart_housekeeping_system`;

-- ============================================================
-- 订单接单 / 指派 / 拒单 / 退款流程改造
-- 增量 DDL，仅执行一次（MySQL 8 的 ADD COLUMN 不支持 IF NOT EXISTS）
-- 注意：不新增订单状态码，复用 6（待退款）/ 7（已取消）
-- ============================================================

-- 1) 订单级「该谁接」+ 指派时间 + 一级提醒的幂等标记
--    说明：orders.housekeeper_id 表示「这单该谁接」，两类订单都会有值：
--          家政员订单在下单时写入（= 明细里的那位），服务订单在员工指派时写入。
--          它不用于区分订单类型 —— 分类仍然只看 order_detail.housekeeper_id。
ALTER TABLE `orders`
  ADD COLUMN `housekeeper_id` bigint DEFAULT NULL COMMENT '该订单由哪位家政员接单（用户指定或员工指派）' AFTER `housekeeper_schedule_id`,
  ADD COLUMN `assign_time` datetime DEFAULT NULL COMMENT '员工指派时刻（一级提醒计时起点）' AFTER `housekeeper_id`,
  ADD COLUMN `remind_time` datetime DEFAULT NULL COMMENT '一级提醒已推送时刻（幂等标记，防重复推送）' AFTER `assign_time`,
  ADD KEY `idx_order_housekeeper` (`housekeeper_id`);

-- 2) 退款原因
--    cancel_reason 语义保持不变：记录「为什么变成了待退款(6)」（超时/员工取消/家政员拒单/用户申请）
--    refund_reason 记录「员工端退款时填写的原因」，仅在 6 -> 7 时写入
ALTER TABLE `orders`
  ADD COLUMN `refund_reason` varchar(255) DEFAULT NULL COMMENT '退款原因（员工端退款到已取消时填写）' AFTER `cancel_reason`;

-- 3) 回填历史家政员订单到订单级字段
--    （家政员订单总是只有 1 条明细，用 MIN 聚合即可）
UPDATE `orders` o
JOIN (
  SELECT order_id, MIN(housekeeper_id) AS hid
  FROM `order_detail`
  WHERE housekeeper_id IS NOT NULL
  GROUP BY order_id
) d ON d.order_id = o.id
SET o.housekeeper_id = d.hid
WHERE o.housekeeper_id IS NULL;

-- 4) 待接单订单扫描的辅助索引
ALTER TABLE `orders` ADD KEY `idx_status_schedule` (`status`, `schedule_date`);

-- 5) 修正 housekeeper_schedule 的错误列注释（仅注释，不改数据、不改语义）
--    运行时真实语义：1 = 可约(空闲)，0 = 已占用
--    依据：HousekeeperScheduleMapper.occupy/release/getAvailableByHousekeeperId、
--          HousekeeperScheduleManagement.vue 的标签、HousekeeperScheduleServiceImpl.save 默认值
ALTER TABLE `housekeeper_schedule`
  MODIFY COLUMN `status` int DEFAULT '1' COMMENT '1可约(空闲) 0已占用';
