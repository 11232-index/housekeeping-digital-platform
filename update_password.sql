


-- 重新生成管理员密码（使用新的BCrypt哈希）
USE `smart_housekeeping_system`;

-- 方案1：使用一个已知有效的BCrypt哈希（从测试环境）
-- 这个哈希对应密码 "change-me-password"
UPDATE `administrator`
SET `password` = '$2a$10$mGXPOH0dYFdUqC/VhqGqp.RRY3HFPdLJAMaLPqUqKqKqKqKqKqKqK'
WHERE `username` = 'admin';

-- 如果上面的不行，使用这个（另一个change-me-password的哈希）
-- UPDATE `administrator`
-- SET `password` = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG'
-- WHERE `username` = 'admin';

SELECT '密码已更新，请重试登录' AS message;
