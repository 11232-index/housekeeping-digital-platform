USE `smart_housekeeping_system`;
DROP TABLE IF EXISTS `feed`;
CREATE TABLE IF NOT EXISTS `feed` (
    `id`                BIGINT  NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `updates_id`        BIGINT                        COMMENT '关联updates表id',
    `service_intro_id`  BIGINT                        COMMENT '关联service_intro表id',
    `create_time`       DATETIME                      COMMENT '创建时间',
    `update_time`       DATETIME                      COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_updates` (`updates_id`),
    KEY `idx_service_intro` (`service_intro_id`),
    KEY `idx_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资讯动态统一表（联表）';
