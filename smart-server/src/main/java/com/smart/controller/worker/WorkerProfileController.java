package com.smart.controller.worker;

import com.smart.context.BaseContext;
import com.smart.result.Result;
import com.smart.service.WorkerProfileService;
import com.smart.vo.WorkerProfileVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 家政员端"我的资料"（只读）
 * <p>
 * 认证：/worker/** 已纳入 JwtTokenEmployeeInterceptor，家政员登录签发的就是员工令牌（claims 存 empId），
 * 所以这里能直接取到 BaseContext 里的 employeeId。
 * <p>
 * 注意：刻意不加 @RoleCheck —— 该注解默认只放行 admin/manager，而家政员的 role 是 cleaner，
 * 加了必然 403。不带注解即"只要求令牌有效"，登录时已保证 role = cleaner。
 */
@RestController
@RequestMapping("/worker/profile")
@Slf4j
public class WorkerProfileController {

    @Autowired
    private WorkerProfileService workerProfileService;

    /**
     * 查询本人的资料、排班与可预约时段
     */
    @GetMapping
    public Result<WorkerProfileVO> get() {
        Long employeeId = BaseContext.getCurrentId();
        log.info("家政员端查询我的资料：employeeId={}", employeeId);
        return Result.success(workerProfileService.getProfile(employeeId));
    }
}
