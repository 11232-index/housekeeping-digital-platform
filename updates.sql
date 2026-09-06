USE `smart_housekeeping_system`;
DROP TABLE IF EXISTS `updates`;
CREATE TABLE IF NOT EXISTS `updates` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `title`       VARCHAR(100) NOT NULL              COMMENT '动态标题',
    `picture`     VARCHAR(500)                       COMMENT '图片路径',
    `content`     TEXT                               COMMENT '动态内容',
    `create_user` BIGINT                             COMMENT '创建者',
    `update_user` BIGINT                             COMMENT '更新者',
    `create_time` DATETIME                           COMMENT '创建时间',
    `update_time` DATETIME                           COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态消息表';
