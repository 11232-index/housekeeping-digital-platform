package com.smart.controller.user;

import com.smart.constant.MessageConstant;
import com.smart.context.BaseContext;
import com.smart.dto.OrderCancelDTO;
import com.smart.dto.OrderItemDTO;
import com.smart.dto.OrderPageQueryDTO;
import com.smart.dto.OrderSubmitDTO;
import com.smart.entity.Order;
import com.smart.result.Result;
import com.smart.service.OrderNotifyService;
import com.smart.service.OrderService;
import com.smart.vo.OrderSubmitVO;
import com.smart.vo.OrderVO;
import com.github.pagehelper.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * C端 - 订单接口
 */
@RestController
@RequestMapping("/user/order")
@Slf4j
public class OrderUserController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderNotifyService orderNotifyService;

    /**
     * 用户提交订单
     */
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrderSubmitDTO dto) {
        log.info("用户提交订单：{}", dto);
        OrderSubmitVO result = orderService.submitOrder(dto);
        // 来单提醒由 MQ 消费者统一处理
        return Result.success(result);
    }

    /**
     * 用户分页查询自己的订单
     */
    @GetMapping("/page")
    public Result<Page<OrderVO>> page(OrderPageQueryDTO dto) {
        log.info("用户查询订单列表：page={}, pageSize={}, status={}", dto.getPage(), dto.getPageSize(), dto.getStatus());
        Page<OrderVO> page = orderService.pageQuery(dto);
        return Result.success(page);
    }

    /**
     * 查询订单详情（含明细列表）
     */
    @GetMapping("/{id}")
    public Result<OrderVO> getById(@PathVariable Long id) {
        log.info("用户查询订单详情：id={}", id);
        OrderVO vo = orderService.getOrderById(id);
        if (vo == null) {
            return Result.error(MessageConstant.SERVICE_ORDER_NOT_FOUND);
        }
        // 归属校验：只允许查看自己的订单；他人的订单统一按“不存在”返回，避免泄露订单归属
        Long userId = BaseContext.getCurrentId();
        if (userId == null || !userId.equals(vo.getUserId())) {
            return Result.error(MessageConstant.SERVICE_ORDER_NOT_FOUND);
        }
        return Result.success(vo);
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrderCancelDTO dto) {
        log.info("用户取消订单：{}", dto);
        orderService.cancelOrder(dto);
        return Result.success();
    }

    /**
     * 支付订单
     */
    @PutMapping("/pay/{id}")
    public Result pay(@PathVariable Long id, @RequestParam(required = false) Integer payMethod) {
        log.info("用户支付订单：id={}, payMethod={}", id, payMethod);
        orderService.payOrder(id, payMethod);
        return Result.success();
    }

    /**
     * 用户提交单条服务预约（仅一条明细，删除购物车对应项）
     */
    @PostMapping("/submit-item")
    public Result<OrderSubmitVO> submitItem(@RequestBody OrderSubmitDTO dto) {
        log.info("用户提交单条预约：{}", dto);
        OrderSubmitVO result = orderService.submitSingleOrder(dto, dto.getItem());
        // 来单提醒由 MQ 消费者统一处理
        return Result.success(result);
    }

    /**
     * 客户催单：推送给在线 Web 员工端（仅允许催自己且处于“待接单/已接单/服务中”的订单）
     */
    @PostMapping("/{id}/remind")
    public Result remind(@PathVariable Long id) {
        log.info("客户催单：orderId={}", id);
        OrderVO vo = orderService.getOrderById(id);
        if (vo == null) {
            return Result.error(MessageConstant.SERVICE_ORDER_NOT_FOUND);
        }
        Long userId = BaseContext.getCurrentId();
        if (userId == null || !userId.equals(vo.getUserId())) {
            return Result.error("只能催促自己的订单");
        }
        Integer status = vo.getStatus();
        boolean remindable = Order.STATUS_PENDING_ACCEPT.equals(status)
                || Order.STATUS_ACCEPTED.equals(status)
                || Order.STATUS_IN_SERVICE.equals(status);
        if (!remindable) {
            return Result.error("当前订单状态不可催单");
        }
        orderNotifyService.push(
                "urge", "客户催单",
                "客户正在催单，订单号 " + vo.getNumber(),
                vo.getId(), vo.getNumber(), vo.getAmount());
        return Result.success();
    }
}
