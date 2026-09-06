package com.smart.annotation;

import com.smart.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解：用于标识方法需要进行公共字段自动填充
 * 使用在 Mapper 层的方法上，通过 AOP 切面自动填充
 * createTime、updateTime、createUser、updateUser 等公共字段
 */
@Target(ElementType.METHOD)          // 该注解只能用在方法上
@Retention(RetentionPolicy.RUNTIME)  // 运行时保留，确保 AOP 切面能读取到
public @interface AutoFill {

    /**
     * 数据库操作类型
     * INSERT：插入操作，填充 createTime、updateTime、createUser、updateUser
     * UPDATE：更新操作，填充 updateTime、updateUser
     */
    OperationType value();
}
