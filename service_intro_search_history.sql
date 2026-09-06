USE `smart_housekeeping_system`;
DROP TABLE IF EXISTS `service_intro_search_history`;
CREATE TABLE IF NOT EXISTS `service_intro_search_history` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `user_id`     BIGINT       NOT NULL                COMMENT '用户id',
    `keyword`     VARCHAR(100) NOT NULL                COMMENT '搜索关键词',
    `create_time` DATETIME                             COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务介绍搜索记录表';
