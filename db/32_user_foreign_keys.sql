USE `smart_housekeeping_system`;

-- 用户外键约束（必须在 00~31 全部导入之后执行）
-- 作用：orders / address_book 的 user_id 不允许为空，且必须是 user 表里真实存在的用户，
--      从数据库层面杜绝“无主订单 / 无主地址”。
-- 删除有订单或地址的用户会被拦住（ON DELETE RESTRICT）。

ALTER TABLE `orders`
  ADD CONSTRAINT `fk_orders_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
  ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `address_book`
  ADD CONSTRAINT `fk_address_book_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
  ON DELETE RESTRICT ON UPDATE RESTRICT;
