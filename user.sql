-- 1. 选中你的数据库
USE `smart_housekeeping_system`;

-- 2. 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
                                      `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                      `openid` VARCHAR(45) NOT NULL COMMENT '微信用户的唯一标识',
                                      `name` VARCHAR(32) DEFAULT NULL COMMENT '用户姓名',
                                      `phone` VARCHAR(11) DEFAULT NULL COMMENT '手机号',
                                      `sex` VARCHAR(2) DEFAULT NULL COMMENT '性别',
                                      `id_number` VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
                                      `avatar` VARCHAR(500) DEFAULT NULL COMMENT '微信用户头像路径',
                                      `create_time` DATETIME DEFAULT NULL COMMENT '注册时间',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_openid` (`openid`) -- 确保每个微信用户的openid唯一
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';