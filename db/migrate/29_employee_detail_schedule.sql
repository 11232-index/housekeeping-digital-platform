USE `smart_housekeeping_system`;

-- employee_detail 新增"排班"字段：存员工的每周固定排班
-- 格式：JSON，key 是星期（1=周一 … 7=周日），value 是该天自己的一条或多条时段
-- 例：{"1":[{"start":"09:00","end":"11:00"}],"3":[{"start":"14:00","end":"16:00"},{"start":"18:00","end":"20:00"}]}
-- 没有 key 的星期表示不排班；无排班存空字符串 ''（管理端保存时写入，非 NULL，便于清空后落库）
ALTER TABLE `employee_detail`
  ADD COLUMN `schedule` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs
  COMMENT '排班（JSON：星期 -> 该天时段列表）' AFTER `work_status`;
