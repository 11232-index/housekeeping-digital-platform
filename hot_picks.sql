USE `smart_housekeeping_system`;
DROP TABLE IF EXISTS `hot_picks`;
CREATE TABLE IF NOT EXISTS `hot_picks` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `name`        VARCHAR(100) NOT NULL              COMMENT '服务名称',
    `picture`     VARCHAR(500)  NOT NULL             COMMENT '服务推荐图片路径',
    `status`      INT          DEFAULT 0             COMMENT '状态 0关闭 1启用',
    `create_user` BIGINT                             COMMENT '创建者',
    `update_user` BIGINT                             COMMENT '更新者',
    `sort_order`  INT          DEFAULT 0             COMMENT '排序',
    `create_time` DATETIME                           COMMENT '创建时间',
    `update_time` DATETIME                           COMMENT '更新时间',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务推荐表';
