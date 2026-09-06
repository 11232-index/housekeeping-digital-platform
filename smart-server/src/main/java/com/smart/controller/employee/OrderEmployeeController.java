package com.smart.controller.employee;

import com.smart.annotation.RoleCheck;
import com.smart.constant.MessageConstant;
import com.smart.dto.OrderDTO;
import com.smart.dto.OrderPageQueryDTO;
import com.smart.result.Result;
import com.smart.service.OrderService;
import com.smart.vo.OrderVO;
import com.github.pagehelper.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Result<Page<OrderVO>> page(OrderPageQueryDTO dto) {
        log.info("员工端查询订单列表：page={}, pageSize={}, status={}, userId={}",
                dto.getPage(), dto.getPageSize(), dto.getStatus(), dto.getUserId());
        Page<OrderVO> page = orderService.adminPageQuery(dto);
        return Result.success(page);
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
     * 更新订单状态（接单/开始服务/完成）
     */
    @PutMapping("/status/{id}")
    @RoleCheck({"admin", "manager"})
    public Result updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        log.info("员工端更新订单状态：id={}, status={}", id, status);
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
}
