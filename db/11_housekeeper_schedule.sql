USE `smart_housekeeping_system`;
CREATE TABLE `housekeeper_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `housekeeper_id` bigint NOT NULL COMMENT '关联家政员表id',
  `schedule_date` date NOT NULL COMMENT '预约日期 如2026-06-12',
  `start_time` time NOT NULL COMMENT '开始时间 如14:00:00',
  `end_time` time NOT NULL COMMENT '结束时间 如18:00:00',
  `status` int DEFAULT '1' COMMENT '1可约(空闲) 0已占用',
  `create_user` bigint DEFAULT NULL COMMENT '创建者',
  `update_user` bigint DEFAULT NULL COMMENT '更新者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_housekeeper` (`housekeeper_id`),
  KEY `idx_date` (`schedule_date`)
) ENGINE=InnoDB AUTO_INCREMENT=80 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='家政员预约时间表';
