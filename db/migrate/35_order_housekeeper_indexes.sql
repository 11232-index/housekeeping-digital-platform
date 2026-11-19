USE `smart_housekeeping_system`;

-- 仅对「已经存在数据的旧库」执行一次；全新导入 db/00~32 已包含这些索引，不要重复执行。

-- 按档期 / 地址查订单时走索引，避免全表扫描
ALTER TABLE `orders` ADD INDEX `idx_order_schedule` (`housekeeper_schedule_id`);
ALTER TABLE `orders` ADD INDEX `idx_order_address` (`address_book_id`);

-- 一个员工只能有一份家政员档案（执行前确认没有重复 employee_id）
-- 查重：SELECT employee_id, COUNT(*) FROM housekeeper GROUP BY employee_id HAVING COUNT(*) > 1;
ALTER TABLE `housekeeper` ADD UNIQUE KEY `uk_employee_id` (`employee_id`);
-- 唯一索引已覆盖 employee_id 的查询，删掉旧的普通索引避免重复
ALTER TABLE `housekeeper` DROP INDEX `idx_employee_id`;
