USE `smart_housekeeping_system`;
DROP TABLE IF EXISTS `about`;
CREATE TABLE IF NOT EXISTS `about` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `title`       VARCHAR(100) NOT NULL              COMMENT '题目',
    `sort_order`  INT          DEFAULT 0             COMMENT '排序',
    `content`     TEXT                               COMMENT '具体内容',
    `create_user` BIGINT                             COMMENT '创建者',
    `update_user` BIGINT                             COMMENT '更新者',
    `create_time` DATETIME                           COMMENT '创建时间',
    `update_time` DATETIME                           COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关于我们表';
