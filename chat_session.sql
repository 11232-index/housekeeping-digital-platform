-- 使用数据库
USE `smart_housekeeping_system`;

-- 创建AI对话会话记录表
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `admin_id` bigint NOT NULL COMMENT '管理员ID',
  `title` varchar(200) COLLATE utf8_bin NOT NULL COMMENT '会话标题（第一个问题）',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='AI对话会话记录';
