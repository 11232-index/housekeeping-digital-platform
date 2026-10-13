package com.smart.controller.employee;

import com.smart.annotation.RoleCheck;
import com.smart.constant.MessageConstant;
import com.smart.dto.OrderAssignDTO;
import com.smart.dto.OrderCancelDTO;
import com.smart.dto.OrderDTO;
import com.smart.dto.OrderPageQueryDTO;
import com.smart.dto.OrderRefundDTO;
import com.smart.result.PageResult;
import com.smart.result.Result;
import com.smart.service.OrderService;
import com.smart.vo.HousekeeperCandidateVO;
import com.smart.vo.OrderVO;
import com.github.pagehelper.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工端 - 订单管理接口
 */
@RestController
@RequestMapping("/employee/order")
@Slf4j
public class OrderEmployeeController {

    @Autowired
    private OrderService orderService;

    /**
     * 分页查询所有订单
     */
    @GetMapping("/page")
    @RoleCheck({"admin", "manager"})
    public Result<PageResult> page(OrderPageQueryDTO dto) {
        log.info("员工端查询订单列表：page={}, pageSize={}, status={}, userId={}, phone={}, beginTime={}, endTime={}",
                dto.getPage(), dto.getPageSize(), dto.getStatus(), dto.getUserId(),
                dto.getPhone(), dto.getBeginTime(), dto.getEndTime());
        Page<OrderVO> page = orderService.adminPageQuery(dto);
        return Result.success(new PageResult(page.getTotal(), page.getResult()));
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/{id}")
    @RoleCheck({"admin", "manager"})
    public Result<OrderVO> getById(@PathVariable Long id) {
        log.info("员工端查询订单详情：id={}", id);
        OrderVO vo = orderService.getOrderById(id);
        if (vo == null) {
            return Result.error(MessageConstant.SERVICE_ORDER_NOT_FOUND);
        }
        return Result.success(vo);
    }

    /**
     * 推进订单状态（白名单：3→4 开始服务、4→5 完成）
     * <p>
     * 「待接单→已接单」走指派/接单接口（需要占用家政员时段），
     * 「转入待退款/已取消」各有带原因的独立入口，都不能从这里绕过。
     */
    @PutMapping("/status/{id}")
    @RoleCheck({"admin", "manager"})
    public Result updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        log.info("员工端推进订单状态：id={}, status={}", id, status);
        orderService.updateOrderStatus(id, status);
        return Result.success();
    }

    /**
     * 设置预计上门时间
     */
    @PutMapping("/estimated-time/{id}")
    @RoleCheck({"admin", "manager"})
    public Result updateEstimatedTime(@PathVariable Long id, @RequestBody OrderDTO dto) {
        log.info("员工端设置预计上门时间：id={}, dto={}", id, dto);
        orderService.updateEstimatedServiceTime(id, dto);
        return Result.success();
    }

    /**
     * 查询可指派的家政员候选（限定「该订单服务时段内空闲」的家政员）
     */
    @GetMapping("/candidates")
    @RoleCheck({"admin", "manager"})
    public Result<List<HousekeeperCandidateVO>> candidates(@RequestParam Long orderId) {
        log.info("员工端查询可指派家政员候选：orderId={}", orderId);
        return Result.success(orderService.listAssignCandidates(orderId));
    }

    /**
     * 指派家政员（服务订单专用）。
     * 成功后会占用该家政员此时段并推送通知，订单仍停在「待接单」等家政员确认。
     */
    @PostMapping("/{id}/assign")
    @RoleCheck({"admin", "manager"})
    public Result assign(@PathVariable Long id, @RequestBody OrderAssignDTO dto) {
        log.info("员工端指派家政员：orderId={}, housekeeperId={}", id, dto.getHousekeeperId());
        orderService.assignOrder(id, dto.getHousekeeperId());
        return Result.success();
    }

    /**
     * 取消待接单订单（必填取消原因）→ 待退款
     */
    @PostMapping("/{id}/cancel")
    @RoleCheck({"admin", "manager"})
    public Result cancel(@PathVariable Long id, @RequestBody OrderCancelDTO dto) {
        log.info("员工端取消订单：orderId={}, cancelReason={}", id, dto.getCancelReason());
        orderService.staffCancelOrder(id, dto.getCancelReason());
        return Result.success();
    }

    /**
     * 确认退款（必填退款原因）：待退款 → 已取消
     */
    @PostMapping("/{id}/refund")
    @RoleCheck({"admin", "manager"})
    public Result refund(@PathVariable Long id, @RequestBody OrderRefundDTO dto) {
        log.info("员工端确认退款：orderId={}, refundReason={}", id, dto.getRefundReason());
        orderService.completeRefund(id, dto.getRefundReason());
        return Result.success();
    }
}
