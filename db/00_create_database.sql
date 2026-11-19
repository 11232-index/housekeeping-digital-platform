-- 第 0 步：建库。后面的 01~31 建表文件都默认这个库已存在。
-- 各表的字符集/排序规则在各自表定义里已逐个显式声明，这里的库默认值只影响没写明的表。
CREATE DATABASE IF NOT EXISTS `smart_housekeeping_system`
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs;
USE `smart_housekeeping_system`;
