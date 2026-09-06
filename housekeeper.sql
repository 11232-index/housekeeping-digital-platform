USE `smart_housekeeping_system`;
DROP TABLE IF EXISTS `housekeeper`;
CREATE TABLE IF NOT EXISTS `housekeeper` (
    `id`           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `employee_id`  BIGINT         NOT NULL                COMMENT '关联员工表id',
    `name`         VARCHAR(50)    NOT NULL                COMMENT '家政员名字',
    `picture`      VARCHAR(500)                           COMMENT '图片路径',
    `price`        DECIMAL(10,2)  DEFAULT 0.00            COMMENT '单价',
    `rating`       DECIMAL(2,1)   DEFAULT 0.0             COMMENT '评分',
    `rank`         INT            DEFAULT 0               COMMENT '身价 0无牌 1金牌 2银牌 3铜牌',
    `carousel`     TEXT                                  COMMENT '轮播图（多图URL，分号分隔）',
    `review`       TEXT                                  COMMENT '综合评价',
    `create_user`  BIGINT                                COMMENT '创建者',
    `update_user`  BIGINT                                COMMENT '更新者',
    `create_time`  DATETIME                              COMMENT '创建时间',
    `update_time`  DATETIME                              COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_employee_id` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政员表';
