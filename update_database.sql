-- 使用数据库
USE `smart_housekeeping_system`;

-- 1. 删除employee表中的所有数据
DELETE FROM `employee`;

-- 2. 创建administrator表（结构与employee表相同）
DROP TABLE IF EXISTS `administrator`;
CREATE TABLE `administrator` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) COLLATE utf8_bin NOT NULL COMMENT '姓名',
  `username` varchar(32) COLLATE utf8_bin NOT NULL COMMENT '用户名',
  `password` varchar(64) COLLATE utf8_bin NOT NULL COMMENT '密码',
  `phone` varchar(11) COLLATE utf8_bin NOT NULL COMMENT '手机号',
  `sex` varchar(2) COLLATE utf8_bin NOT NULL COMMENT '性别',
  `age` varchar(4) COLLATE utf8_bin DEFAULT NULL COMMENT '年龄',
  `id_number` varchar(18) COLLATE utf8_bin NOT NULL COMMENT '身份证号',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态 0:禁用，1:启用',
  `security_question` varchar(100) DEFAULT NULL COMMENT '密保问题',
  `security_answer` varchar(100) DEFAULT NULL COMMENT '密保答案',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='管理员信息';

-- 3. 插入管理员数据（用户名：admin，密码：change-me-password）
-- 注意：密码使用BCrypt加密，这里使用的是change-me-password的BCrypt哈希值
INSERT INTO `administrator` VALUES (
  1,
  'admin',
  'admin',
  '$2a$10$Nasl3Zli2I6MK4oyPnT.5epXPjHreex/fbw3w/4UW2EEkhzQPlL1W',
  '13800138000',
  '1',
  NULL,
  '110101199001010001',
  1,
  NOW(),
  NOW(),
  1,
  1
);

-- 完成提示
SELECT '数据库操作完成：' AS message;
SELECT '1. employee表已清空' AS step1;
SELECT '2. administrator表已创建' AS step2;
SELECT '3. 管理员账号已添加（用户名：admin，密码：change-me-password）' AS step3;

UPDATE `administrator` SET `password` = '$2a$10$Nasl3Zli2I6MK4oyPnT.5epXPjHreex/fbw3w/4UW2EEkhzQPlL1W' WHERE `username` = 'admin';


ALTER TABLE `administrator` ADD COLUMN `security_question` varchar(100) DEFAULT NULL COMMENT '密保问题' AFTER `status`;
ALTER TABLE `administrator` ADD COLUMN `security_answer` varchar(100) DEFAULT NULL COMMENT '密保答案' AFTER `security_question`;
UPDATE `administrator`
SET `security_question` = '您的用户名是什么？',
    `security_answer` = '$2a$10$V2b1puFhznmYRgTirl4WjeYPhLkfa2KrcMjpwQwRJinWwAFbculSS'
WHERE `username` = 'admin';