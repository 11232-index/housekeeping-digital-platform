package com.smart.controller.user;

import com.smart.constant.MessageConstant;
import com.smart.dto.OrderCancelDTO;
import com.smart.dto.OrderItemDTO;
import com.smart.dto.OrderPageQueryDTO;
import com.smart.dto.OrderSubmitDTO;
import com.smart.result.Result;
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

    /**
     * 用户提交订单
     */
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrderSubmitDTO dto) {
        log.info("用户提交订单：{}", dto);
        OrderSubmitVO result = orderService.submitOrder(dto);
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
        return Result.success(result);
    }
}
