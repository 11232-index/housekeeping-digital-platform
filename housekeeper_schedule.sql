USE `smart_housekeeping_system`;
DROP TABLE IF EXISTS `housekeeper_schedule`;
CREATE TABLE IF NOT EXISTS `housekeeper_schedule` (
    `id`              BIGINT  NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `housekeeper_id`  BIGINT  NOT NULL                COMMENT '关联家政员表id',
    `schedule_date`   DATE    NOT NULL                COMMENT '预约日期 如2026-06-12',
    `start_time`      TIME    NOT NULL                COMMENT '开始时间 如14:00:00',
    `end_time`        TIME    NOT NULL                COMMENT '结束时间 如18:00:00',
    `status`          INT     DEFAULT 1               COMMENT '1可约 0不可约',
    `create_user`     BIGINT                          COMMENT '创建者',
    `update_user`     BIGINT                          COMMENT '更新者',
    `create_time`     DATETIME                        COMMENT '创建时间',
    `update_time`     DATETIME                        COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_housekeeper` (`housekeeper_id`),
    KEY `idx_date` (`schedule_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政员预约时间表';
