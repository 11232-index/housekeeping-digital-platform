package com.smart.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限校验注解
 * 标注在 Controller 方法上，指定哪些角色可以访问
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RoleCheck {

    /**
     * 允许访问的角色列表
     * 默认 admin 和 manager 可访问
     */
    String[] value() default {"admin", "manager"};
}
