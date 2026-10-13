package com.smart.controller.worker;

import com.github.pagehelper.Page;
import com.smart.annotation.RoleCheck;
import com.smart.context.BaseContext;
import com.smart.dto.WorkerOrderPageQueryDTO;
import com.smart.result.Result;
import com.smart.service.WorkerOrderService;
import com.smart.vo.WorkerOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 家政员端 - 我的订单
 * <p>
 * 认证：/worker/** 已纳入 JwtTokenEmployeeInterceptor，家政员登录签发的是员工令牌（claims 存 empId），
 * 这里直接取 BaseContext 里的 employeeId，再由 service 反查 housekeeper 档案。
 * <p>
 * 说明：这里显式写 @RoleCheck({"cleaner"}) —— 注解的 value 就是允许的角色列表，
 * cleaner 是 employee.role 的合法取值，切面用 contains 判断，能正确放行。
 */
@RestController
@RequestMapping("/worker/order")
@Slf4j
public class WorkerOrderController {

    @Autowired
    private WorkerOrderService workerOrderService;

    /**
     * 我的订单分页查询（小程序进入页面时拉取）。
     * <p>
     * 这里直接返回 Page 本身（序列化成 JSON 数组），与用户端 /user/order/page 保持一致 ——
     * 小程序端是照 historyOrder 那套写的，靠 res.data.length 判断，吃的是裸数组；
     * 若返回 PageResult{total,records}，前端会解析成空列表。
     */
    @GetMapping("/page")
    @RoleCheck({"cleaner"})
    public Result<Page<WorkerOrderVO>> page(WorkerOrderPageQueryDTO dto) {
        Page<WorkerOrderVO> page = workerOrderService.pageQuery(BaseContext.getCurrentId(), dto);
        return Result.success(page);
    }

    /**
     * 待接单数量（小程序首页角标）
     */
    @GetMapping("/pending-count")
    @RoleCheck({"cleaner"})
    public Result<Long> pendingCount() {
        return Result.success(workerOrderService.countPending(BaseContext.getCurrentId()));
    }

    /**
     * 订单详情（只能查指派给自己的）
     */
    @GetMapping("/{id}")
    @RoleCheck({"cleaner"})
    public Result<WorkerOrderVO> getById(@PathVariable Long id) {
        return Result.success(workerOrderService.getById(BaseContext.getCurrentId(), id));
    }

    /**
     * 接单：待接单 → 已接单（必须填写预计到达时间）
     */
    @PostMapping("/{id}/accept")
    @RoleCheck({"cleaner"})
    public Result accept(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String estimatedArrivalTime = body == null ? null : body.get("estimatedArrivalTime");
        log.info("家政员接单：orderId={}, 预计到达={}", id, estimatedArrivalTime);
        workerOrderService.accept(BaseContext.getCurrentId(), id, estimatedArrivalTime);
        return Result.success();
    }

    /**
     * 确认到达：已接单 → 服务中（由 SQL 写入实际上门时间）
     */
    @PostMapping("/{id}/arrive")
    @RoleCheck({"cleaner"})
    public Result arrive(@PathVariable Long id) {
        log.info("家政员确认到达：orderId={}", id);
        workerOrderService.arrive(BaseContext.getCurrentId(), id);
        return Result.success();
    }

    /**
     * 完成服务：服务中 → 已完成
     */
    @PostMapping("/{id}/complete")
    @RoleCheck({"cleaner"})
    public Result complete(@PathVariable Long id) {
        log.info("家政员完成服务：orderId={}", id);
        workerOrderService.complete(BaseContext.getCurrentId(), id);
        return Result.success();
    }

    /**
     * 不接单：退回「待接单」（必须填写原因），由员工端决定重新指派或取消
     */
    @PostMapping("/{id}/reject")
    @RoleCheck({"cleaner"})
    public Result reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        log.info("家政员不接单：orderId={}, reason={}", id, reason);
        workerOrderService.reject(BaseContext.getCurrentId(), id, reason);
        return Result.success();
    }

    /**
     * 上报「无法服务」：已接单/服务中 → 待退款（必须填写原因）
     */
    @PostMapping("/{id}/cannot-serve")
    @RoleCheck({"cleaner"})
    public Result cannotServe(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        log.info("家政员上报无法服务：orderId={}, reason={}", id, reason);
        workerOrderService.cannotServe(BaseContext.getCurrentId(), id, reason);
        return Result.success();
    }
}
