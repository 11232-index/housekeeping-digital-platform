USE smart_housekeeping_system;

-- ============================================================
-- 数据迁移：旧三态status → 新二态status
-- ============================================================
-- 映射逻辑：
--   旧(前端显示)       →  新(前后端统一)
--   status=0 可预约     →  status=1 可预约
--   status=1 服务中     →  status=0 休息中（"服务中"状态已移除）
--   status=2 休息中     →  status=0 休息中
-- ============================================================
-- 使用 CASE WHEN 一次性完成，不会出现更新顺序冲突

-- 服务项目
UPDATE service_item
SET status = CASE status
  WHEN 2 THEN 0   -- 旧休息中 → 新休息中
  WHEN 0 THEN 1   -- 旧可预约 → 新可预约
  WHEN 1 THEN 0   -- 旧服务中 → 休息中（此状态已废弃）
  ELSE status
END;

-- 服务套餐
UPDATE service_package
SET status = CASE status
  WHEN 2 THEN 0   -- 旧休息中 → 新休息中
  WHEN 0 THEN 1   -- 旧可预约 → 新可预约
  WHEN 1 THEN 0   -- 旧服务中 → 休息中（此状态已废弃）
  ELSE status
END;

-- 验证结果
-- SELECT id, name, status FROM service_item;
-- SELECT id, name, status FROM service_package;
