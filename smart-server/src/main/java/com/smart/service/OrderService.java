package com.smart.service;

import com.smart.dto.OrderCancelDTO;
import com.smart.dto.OrderDTO;
import com.smart.dto.OrderItemDTO;
import com.smart.dto.OrderPageQueryDTO;
import com.smart.dto.OrderSubmitDTO;
import com.smart.vo.HousekeeperCandidateVO;
import com.smart.vo.OrderSubmitVO;
import com.smart.vo.OrderVO;
import com.github.pagehelper.Page;

import java.util.List;

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

    /**
     * 员工端查询可指派的家政员候选
     * @param orderId 订单id（必须是待接单且尚未指派的服务订单）
     * @return 该订单服务时段内空闲的家政员列表
     */
    List<HousekeeperCandidateVO> listAssignCandidates(Long orderId);

    /**
     * 员工端指派家政员：写入订单级家政员与指派时刻，并占用该家政员此时段。
     * 订单状态不变，仍停在「待接单(2)」等家政员确认。
     * @param orderId 订单id
     * @param housekeeperId 家政员id
     */
    void assignOrder(Long orderId, Long housekeeperId);

    /**
     * 员工端取消待接单订单：置为「待退款(6)」并记录取消原因，同时释放已占用时段
     * @param orderId 订单id
     * @param cancelReason 取消原因（必填）
     */
    void staffCancelOrder(Long orderId, String cancelReason);

    /**
     * 员工端确认退款：待退款(6) → 已取消(7)，记录退款原因，支付状态置为「退款」
     * @param orderId 订单id
     * @param refundReason 退款原因（必填）
     */
    void completeRefund(Long orderId, String refundReason);
}
