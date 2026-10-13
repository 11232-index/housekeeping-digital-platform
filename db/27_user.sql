USE `smart_housekeeping_system`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `openid` varchar(45) NOT NULL COMMENT '微信用户的唯一标识',
  `name` varchar(32) DEFAULT NULL COMMENT '用户姓名',
  `phone` varchar(11) DEFAULT NULL COMMENT '手机号',
  `sex` varchar(2) DEFAULT NULL COMMENT '性别',
  `id_number` varchar(18) DEFAULT NULL COMMENT '身份证号',
  `avatar` varchar(500) DEFAULT NULL COMMENT '微信用户头像路径',
  `create_time` datetime DEFAULT NULL COMMENT '注册时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB AUTO_INCREMENT=2358 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
