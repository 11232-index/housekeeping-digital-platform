package com.smart.service;

import com.github.pagehelper.Page;
import com.smart.dto.WorkerOrderPageQueryDTO;
import com.smart.vo.WorkerOrderVO;

/**
 * 家政员端「我的订单」服务接口
 */
public interface WorkerOrderService {

    /**
     * 分页查询指派给当前家政员的订单
     * @param employeeId 当前登录员工id（token 里的 empId）
     * @param dto 分页参数
     * @return 订单分页结果
     */
    Page<WorkerOrderVO> pageQuery(Long employeeId, WorkerOrderPageQueryDTO dto);

    /**
     * 待接单数量（小程序首页角标）
     * @param employeeId 当前登录员工id
     * @return 待接单订单数
     */
    long countPending(Long employeeId);

    /**
     * 查看订单详情（只能看指派给自己的订单）
     * @param employeeId 当前登录员工id
     * @param orderId 订单id
     * @return 订单VO
     */
    WorkerOrderVO getById(Long employeeId, Long orderId);

    /**
     * 家政员接单：待接单(2) → 已接单(3)，同时记录家政员填写的预计到达时间
     * @param employeeId 当前登录员工id
     * @param orderId 订单id
     * @param estimatedArrivalTime 预计到达时间（必填，支持 yyyy-MM-dd HH:mm / yyyy-MM-dd HH:mm:ss）
     */
    void accept(Long employeeId, Long orderId, String estimatedArrivalTime);

    /**
     * 家政员确认到达：已接单(3) → 服务中(4)，记录实际上门时间
     * @param employeeId 当前登录员工id
     * @param orderId 订单id
     */
    void arrive(Long employeeId, Long orderId);

    /**
     * 家政员完成服务：服务中(4) → 已完成(5)
     * @param employeeId 当前登录员工id
     * @param orderId 订单id
     */
    void complete(Long employeeId, Long orderId);

    /**
     * 家政员不接单：<b>退回待接单(2)</b>（不是转待退款），清空指派并释放已占用时段，
     * 之后由员工端决定重新指派给别的家政员，还是取消订单转待退款。
     * @param employeeId 当前登录员工id
     * @param orderId 订单id
     * @param reason 不接单原因（必填）
     */
    void reject(Long employeeId, Long orderId, String reason);

    /**
     * 家政员上报「无法服务」：已接单(3)/服务中(4) → 待退款(6)。
     * 与「不接单」的区别：不接单是把订单退回待接单等重新指派；
     * 无法服务是接了之后出现突发情况、订单已上路，只能终止走退款。
     * <p>
     * 两种情况都<b>不释放</b>预约时段 —— 家政员做不了，那个时段就该下架，
     * 放回「可约」只会让别的用户再约到同一个没人能做的时段。
     * @param employeeId 当前登录员工id
     * @param orderId 订单id
     * @param reason 原因（必填）
     */
    void cannotServe(Long employeeId, Long orderId, String reason);
}
