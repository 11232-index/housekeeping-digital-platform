USE `smart_housekeeping_system`;
CREATE TABLE `tb_user_voucher` (
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
  KEY `idx_order` (`order_id`),
  -- 按券统计"已发份数"（对账任务、懒预热）以及 voucher_id 单列查询
  KEY `idx_voucher` (`voucher_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs COMMENT='用户优惠券（领取与使用记录）';
