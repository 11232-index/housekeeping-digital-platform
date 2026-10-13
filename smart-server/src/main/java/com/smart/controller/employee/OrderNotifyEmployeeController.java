package com.smart.controller.employee;

import com.smart.annotation.RoleCheck;
import com.smart.context.BaseContext;
import com.smart.result.Result;
import com.smart.service.OrderNotifyService;
import com.smart.vo.OrderNotifyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工端 - 订单提醒消息中心（来单提醒/客户催单，按员工区分未读）
 */
@RestController
@RequestMapping("/employee/order-notify")
@Slf4j
public class OrderNotifyEmployeeController {

    @Autowired
    private OrderNotifyService orderNotifyService;

    /**
     * 最近 N 条消息（含当前员工是否已读）
     */
    @GetMapping("/list")
    @RoleCheck({"admin", "manager"})
    public Result<List<OrderNotifyVO>> list(@RequestParam(defaultValue = "30") int limit) {
        Long employeeId = BaseContext.getCurrentId();
        log.info("员工端查询订单提醒消息：employeeId={}, limit={}", employeeId, limit);
        return Result.success(orderNotifyService.listRecent(employeeId, limit));
    }

    /**
     * 当前员工未读消息数
     */
    @GetMapping("/unread-count")
    @RoleCheck({"admin", "manager"})
    public Result<Long> unreadCount() {
        Long employeeId = BaseContext.getCurrentId();
        return Result.success(orderNotifyService.countUnread(employeeId));
    }

    /**
     * 标记某条消息已读
     */
    @PostMapping("/{id}/read")
    @RoleCheck({"admin", "manager"})
    public Result markRead(@PathVariable Long id) {
        Long employeeId = BaseContext.getCurrentId();
        log.info("员工端标记消息已读：employeeId={}, notifyId={}", employeeId, id);
        orderNotifyService.markRead(employeeId, id);
        return Result.success();
    }

    /**
     * 一键全部已读
     */
    @PostMapping("/read-all")
    @RoleCheck({"admin", "manager"})
    public Result markAllRead() {
        Long employeeId = BaseContext.getCurrentId();
        log.info("员工端一键全部已读：employeeId={}", employeeId);
        orderNotifyService.markAllRead(employeeId);
        return Result.success();
    }
}
