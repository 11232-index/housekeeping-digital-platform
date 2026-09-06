USE `smart_housekeeping_system`;

ALTER TABLE employee ADD COLUMN role VARCHAR(20) DEFAULT 'staff';

ALTER TABLE housekeeper ADD COLUMN carousel TEXT COMMENT '轮播图' AFTER `rank`;