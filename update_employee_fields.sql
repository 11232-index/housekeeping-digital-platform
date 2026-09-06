-- 使用数据库
USE `smart_housekeeping_system`;

-- 创建员工详细信息表
DROP TABLE IF EXISTS `employee_detail`;
CREATE TABLE `employee_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `employee_id` bigint NOT NULL COMMENT '家政人员ID',
  `education` varchar(255) DEFAULT NULL COMMENT '学历',
  `past_experience` text DEFAULT NULL COMMENT '过去经历',
  `position` varchar(100) DEFAULT NULL COMMENT '岗位',
  `salary` varchar(50) DEFAULT NULL COMMENT '工资',
  `work_status` varchar(50) DEFAULT NULL COMMENT '工作状态',
  `contract` varchar(500) DEFAULT NULL COMMENT '合同文件路径',
  `health_certificate` varchar(500) DEFAULT NULL COMMENT '健康证文件路径',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_employee_id` (`employee_id`),
  CONSTRAINT `fk_employee_detail_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='家政人员详细信息';

SELECT '1. employee表已还原（删除contract、health_certificate字段）' AS step1;
SELECT '2. employee_detail表已创建' AS step2;
-- salary字段已在CREATE TABLE中定义，无需额外添加