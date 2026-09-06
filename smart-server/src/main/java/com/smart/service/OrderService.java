package com.smart.service;

import com.smart.dto.OrderCancelDTO;
import com.smart.dto.OrderDTO;
import com.smart.dto.OrderItemDTO;
import com.smart.dto.OrderPageQueryDTO;
import com.smart.dto.OrderSubmitDTO;
import com.smart.vo.OrderSubmitVO;
import com.smart.vo.OrderVO;
import com.github.pagehelper.Page;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 用户提交订单（从购物车读取所有明细，下单后清空购物车）
     * @param dto 下单参数
     * @return 下单结果
     */
    OrderSubmitVO submitOrder(OrderSubmitDTO dto);

    /**
     * 用户提交单条服务预约（仅提交指定的一条明细，删除购物车对应项）
     * @param dto 下单参数
     * @param item 单条明细
     * @return 下单结果
     */
    OrderSubmitVO submitSingleOrder(OrderSubmitDTO dto, OrderItemDTO item);

    /**
     * 用户端分页查询自己的订单
     * @param dto 分页查询参数
     * @return 订单分页结果
     */
    Page<OrderVO> pageQuery(OrderPageQueryDTO dto);

    /**
     * 根据id查询订单详情（含明细列表）
     * @param id 订单id
     * @return 订单VO
     */
    OrderVO getOrderById(Long id);

    /**
     * 用户取消订单
     * @param dto 取消参数
     */
    void cancelOrder(OrderCancelDTO dto);

    /**
     * 用户支付订单
     * @param orderId 订单id
     */
    void payOrder(Long orderId, Integer payMethod);

    /**
     * 管理端分页查询所有订单
     * @param dto 分页查询参数
     * @return 订单分页结果
     */
    Page<OrderVO> adminPageQuery(OrderPageQueryDTO dto);

    /**
     * 管理端更新订单状态（接单/开始服务/完成）
     * @param orderId 订单id
     * @param status 目标状态
     */
    void updateOrderStatus(Long orderId, Integer status);

    /**
     * 管理端设置预计上门时间
     * @param orderId 订单id
     * @param dto 更新参数
     */
    void updateEstimatedServiceTime(Long orderId, OrderDTO dto);

    /**
     * 生成订单号 HS + yyyyMMddHHmmss + 4位随机数
     * @return 订单号
     */
    String generateOrderNumber();
}
