package com.smart.aspect;

import com.smart.annotation.RoleCheck;
import com.smart.context.BaseContext;
import com.smart.entity.Employee;
import com.smart.exception.BaseException;
import com.smart.mapper.EmployeeMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 角色权限校验切面
 * 拦截标注了 @RoleCheck 的方法，校验当前登录员工的角色
 */
@Slf4j
@Aspect
@Component
public class RoleCheckAspect {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 环绕通知：校验 @RoleCheck 注解指定的角色
     */
    @Around("@annotation(com.smart.annotation.RoleCheck)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取注解上的允许角色列表
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RoleCheck roleCheck = signature.getMethod().getAnnotation(RoleCheck.class);
        String[] allowedRoles = roleCheck.value();

        // 2. 获取当前用户 ID 和类型
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            log.warn("权限校验失败：未登录");
            throw new BaseException("未登录，请先登录");
        }

        String userType = BaseContext.getCurrentUserType();

        // 管理员（administrator 表）拥有全部权限，直接放行
        if ("admin".equals(userType)) {
            log.info("管理员权限校验通过，id={}", currentId);
            return joinPoint.proceed();
        }

        // 员工（employee 表）需要校验角色
        Employee employee = employeeMapper.getById(currentId);
        if (employee == null) {
            log.warn("权限校验失败：员工不存在，id={}", currentId);
            throw new BaseException("员工不存在");
        }

        String currentRole = employee.getRole();
        if (currentRole == null) {
            currentRole = "staff";
        }

        // 3. 校验角色
        boolean hasPermission = Arrays.asList(allowedRoles).contains(currentRole);
        if (!hasPermission) {
            log.warn("权限校验失败：员工 {} (role={}) 无权限访问，需要角色：{}", employee.getName(), currentRole, Arrays.toString(allowedRoles));
            throw new BaseException("权限不足，当前角色：" + currentRole + "，需要角色：" + String.join(",", allowedRoles));
        }

        log.info("权限校验通过：员工 {} (role={})", employee.getName(), currentRole);
        // 4. 放行
        return joinPoint.proceed();
    }
}
