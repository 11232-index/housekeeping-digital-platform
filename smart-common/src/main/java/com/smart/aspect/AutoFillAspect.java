package com.smart.aspect;

import com.smart.annotation.AutoFill;
import com.smart.constant.AutoFillConstant;
import com.smart.context.BaseContext;
import com.smart.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * AOP 切面：自动填充公共字段
 * 拦截带有 @AutoFill 注解的方法，通过反射机制
 * 为实体对象的 createTime、updateTime、createUser、updateUser 字段赋值
 */
@Aspect       // 标识这是一个切面类
@Component    // 交给 Spring IOC 容器管理
@Slf4j        // Lombok 日志
public class AutoFillAspect {

    /**
     * 切点：匹配所有带有 @AutoFill 注解的方法
     */
    @Pointcut("@annotation(com.smart.annotation.AutoFill)")
    public void autoFillPointCut() {}

    /**
     * 环绕通知：在目标方法执行前后进行公共字段的自动填充
     *
     * @param joinPoint 连接点，包含目标方法的信息
     * @return 目标方法的返回值
     * @throws Throwable 异常
     */
    @Around("autoFillPointCut()")
    public Object autoFill(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("开始公共字段自动填充...");

        // 1. 获取方法签名，转换为 MethodSignature 以获取方法详细信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 2. 获取方法上的 @AutoFill 注解，从而拿到操作类型
        AutoFill autoFill = method.getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        // 3. 获取目标方法的参数（实体对象）
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            log.warn("方法 {} 没有参数，跳过自动填充", method.getName());
            return joinPoint.proceed();
        }

        // 约定：实体对象作为第一个参数传入 Mapper 方法
        Object entity = args[0];

        // 4. 获取当前时间和当前登录用户 ID
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        // 5. 根据操作类型，利用反射为不同的公共字段赋值
        if (operationType == OperationType.INSERT) {
            // 插入操作：同时填充创建时间和更新时间、创建人和更新人
            try {
                Method setCreateTime = entity.getClass()
                        .getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass()
                        .getMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass()
                        .getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass()
                        .getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 通过反射调用 setter 方法设置值
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (NoSuchMethodException e) {
                log.debug("实体类 {} 缺少公共字段，跳过部分填充", entity.getClass().getName());
            }
        } else if (operationType == OperationType.UPDATE) {
            // 更新操作：仅填充更新时间和更新人
            try {
                Method setUpdateTime = entity.getClass()
                        .getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass()
                        .getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 通过反射调用 setter 方法设置值
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (NoSuchMethodException e) {
                log.debug("实体类 {} 缺少公共字段，跳过部分填充", entity.getClass().getName());
            }
        }

        // 6. 执行原始方法并返回结果
        log.debug("公共字段自动填充完成，操作类型：{}", operationType);
        return joinPoint.proceed();
    }
}
