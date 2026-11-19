package com.smart.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.smart.constant.MessageConstant;
import com.smart.constant.OrderEventConstant;
import com.smart.dto.WorkerOrderPageQueryDTO;
import com.smart.entity.Housekeeper;
import com.smart.entity.Order;
import com.smart.exception.BaseException;
import com.smart.mapper.HousekeeperMapper;
import com.smart.mapper.OrderDetailMapper;
import com.smart.mapper.OrderMapper;
import com.smart.mq.OrderEventPublisher;
import com.smart.service.HousekeeperScheduleService;
import com.smart.service.OrderNotifyService;
import com.smart.service.UserNotifyService;
import com.smart.service.WorkerOrderService;
import com.smart.utils.OrderStatusUtil;
import com.smart.vo.WorkerOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 家政员端「我的订单」实现
 * <p>
 * 业务规则：订单是否属于当前家政员，一律以 orders.housekeeper_id 为准
 * （家政员订单在下单时写入、服务订单在员工指派时写入）。
 */
@Slf4j
@Service
public class WorkerOrderServiceImpl implements WorkerOrderService {

    /** 原因字段对应列为 varchar(255) */
    private static final int MAX_REASON_LENGTH = 255;

    /** 预计到达时间的可接受格式（小程序端 picker 拼出来的是 yyyy-MM-dd HH:mm） */
    private static final List<DateTimeFormatter> ARRIVAL_FORMATS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
    );

    /** 消息文案里展示预计到达时间的格式 */
    private static final DateTimeFormatter ARRIVAL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** 允许上报「无法服务」的订单状态：已接单 / 服务中 */
    private static final List<Integer> CANNOT_SERVE_STATUSES = Arrays.asList(
            Order.STATUS_ACCEPTED, Order.STATUS_IN_SERVICE);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private HousekeeperMapper housekeeperMapper;

    @Autowired
    private HousekeeperScheduleService housekeeperScheduleService;

    @Autowired
    private OrderNotifyService orderNotifyService;

    @Autowired
    private UserNotifyService userNotifyService;

    @Autowired
    private OrderEventPublisher orderEventPublisher;

    @Override
    public Page<WorkerOrderVO> pageQuery(Long employeeId, WorkerOrderPageQueryDTO dto) {
        Long housekeeperId = requireHousekeeperId(employeeId);
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Order> orderPage = (Page<Order>) orderMapper.pageQueryByHousekeeper(housekeeperId, dto.getStatus());

        List<WorkerOrderVO> voList = orderPage.getResult().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        Page<WorkerOrderVO> resultPage = new Page<>();
        BeanUtils.copyProperties(orderPage, resultPage, "result");
        resultPage.addAll(voList);
        log.info("家政员端查询我的订单：housekeeperId={}, status={}, 共{}条",
                housekeeperId, dto.getStatus(), orderPage.getTotal());
        return resultPage;
    }

    @Override
    public long countPending(Long employeeId) {
        return orderMapper.countPendingByHousekeeper(requireHousekeeperId(employeeId));
    }

    @Override
    public WorkerOrderVO getById(Long employeeId, Long orderId) {
        Long housekeeperId = requireHousekeeperId(employeeId);
        Order order = orderMapper.getById(orderId);
        // 不是指派给自己的订单，一律按「不存在」返回，避免泄露他人订单归属
        if (order == null || !housekeeperId.equals(order.getHousekeeperId())) {
            throw new BaseException(MessageConstant.SERVICE_ORDER_NOT_FOUND);
        }
        return toVO(order);
    }

    @Override
    @Transactional
    @CacheEvict(value = "order", key = "'one:' + #orderId")
    public void accept(Long employeeId, Long orderId, String estimatedArrivalTime) {
        Long housekeeperId = requireHousekeeperId(employeeId);
        LocalDateTime estimated = parseEstimatedArrival(estimatedArrivalTime);
        if (estimated == null) {
            throw new BaseException(MessageConstant.ORDER_ESTIMATED_TIME_REQUIRED);
        }

        // CAS：只有「指派给我 且 仍是待接单」才成功，重复点击/已被别人处理都会被拒
        int rows = orderMapper.workerAcceptCas(
                orderId, housekeeperId, Order.STATUS_PENDING_ACCEPT, Order.STATUS_ACCEPTED, estimated);
        if (rows == 0) {
            throw new BaseException(stateChangedMessage(orderId));
        }

        // 通知由 MQ 消费者统一处理
        Order order = orderMapper.getById(orderId);
        if (order != null) {
            order.setStatus(Order.STATUS_ACCEPTED);
            orderEventPublisher.publish(OrderEventConstant.ACCEPTED, order,
                    employeeId, null, estimated, "worker");
        }
        log.info("家政员接单成功：employeeId={}, housekeeperId={}, orderId={}, 预计到达={}",
                employeeId, housekeeperId, orderId, estimated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "order", key = "'one:' + #orderId")
    public void arrive(Long employeeId, Long orderId) {
        Long housekeeperId = requireHousekeeperId(employeeId);
        // 已接单(3) → 服务中(4)，由 SQL 顺带写入实际上门时间
        int rows = orderMapper.workerArriveCas(
                orderId, housekeeperId, Order.STATUS_ACCEPTED, Order.STATUS_IN_SERVICE);
        if (rows == 0) {
            throw new BaseException(stateChangedMessage(orderId));
        }
        log.info("家政员确认到达，订单转入服务中：employeeId={}, orderId={}", employeeId, orderId);
        // 通知由 MQ 消费者统一处理
        Order order = orderMapper.getById(orderId);
        if (order != null) {
            order.setStatus(Order.STATUS_IN_SERVICE);
            orderEventPublisher.publish(OrderEventConstant.ARRIVED, order, employeeId, null, null, "worker");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "order", key = "'one:' + #orderId")
    public void complete(Long employeeId, Long orderId) {
        Long housekeeperId = requireHousekeeperId(employeeId);
        int rows = orderMapper.workerCompleteCas(
                orderId, housekeeperId, Order.STATUS_IN_SERVICE, Order.STATUS_COMPLETED);
        if (rows == 0) {
            throw new BaseException(stateChangedMessage(orderId));
        }
        log.info("家政员完成服务：employeeId={}, orderId={}", employeeId, orderId);
        // 通知由 MQ 消费者统一处理
        Order order = orderMapper.getById(orderId);
        if (order != null) {
            order.setStatus(Order.STATUS_COMPLETED);
            orderEventPublisher.publish(OrderEventConstant.COMPLETED, order, employeeId, null, null, "worker");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "order", key = "'one:' + #orderId")
    public void reject(Long employeeId, Long orderId, String reason) {
        Long housekeeperId = requireHousekeeperId(employeeId);
        String trimmed = requireReason(reason);

        Order order = orderMapper.getById(orderId);
        if (order == null || !housekeeperId.equals(order.getHousekeeperId())) {
            throw new BaseException(MessageConstant.SERVICE_ORDER_NOT_FOUND);
        }

        // 退回「待接单」并清空指派，交给员工端决定重新指派还是取消（不直接转待退款）
        int rows = orderMapper.workerRejectCas(orderId, housekeeperId, Order.STATUS_PENDING_ACCEPT, trimmed);
        if (rows == 0) {
            throw new BaseException(stateChangedMessage(orderId));
        }

        // 注意：这里刻意【不释放】时段。
        // 订单只是退回待接单、并没有终结，员工还要重新指派它。如果这时候放回「可约」，
        // 别的用户会立刻约走这个时段，造成「订单还活着、时段却没了」的冲突。
        // 时段跟着订单走 —— 等订单真正终结（员工取消 / 退款 / 超时自动转待退款）时才释放。

        // 通知员工端：有人拒单，需要重新指派或取消
        orderNotifyService.push("reject", "家政员已拒单",
                "订单 " + order.getNumber() + " 家政员不接单：" + trimmed + "，请重新指派或取消",
                orderId, order.getNumber(), order.getAmount());
        // 同时通知下单用户（订单会由商家重新安排，先安抚一下）
        userNotifyService.push(order.getUserId(), "rejected", "家政员无法接单",
                "您的订单 " + order.getNumber() + " 暂无人接单，我们正在为您重新安排",
                orderId, order.getNumber());
        log.info("家政员拒单成功，订单退回待接单：orderId={}, reason={}", orderId, trimmed);
    }

    @Override
    @Transactional
    @CacheEvict(value = "order", key = "'one:' + #orderId")
    public void cannotServe(Long employeeId, Long orderId, String reason) {
        Long housekeeperId = requireHousekeeperId(employeeId);
        String trimmed = requireReason(reason);

        Order order = orderMapper.getById(orderId);
        if (order == null || !housekeeperId.equals(order.getHousekeeperId())) {
            throw new BaseException(MessageConstant.SERVICE_ORDER_NOT_FOUND);
        }

        // 已接单/服务中 → 待退款，走退款流程（订单已上路，不能像「不接单」那样退回重派）
        int rows = orderMapper.workerCannotServeCas(orderId, housekeeperId,
                CANNOT_SERVE_STATUSES, Order.STATUS_REFUNDING, trimmed);
        if (rows == 0) {
            throw new BaseException(stateChangedMessage(orderId));
        }

        // 同样【不释放】预约时段：家政员都做不了了，放回「可约」只会让别的用户再约到
        // 通知由 MQ 消费者统一处理
        order.setStatus(Order.STATUS_REFUNDING);
        orderEventPublisher.publish(OrderEventConstant.CANCELLED, order, employeeId, trimmed, null, "worker");
        log.info("家政员上报无法服务，订单转入待退款：orderId={}, reason={}", orderId, trimmed);
    }

    /**
     * 取当前登录员工对应的家政员档案。
     * 家政员登录签发的令牌里存的是 empId，需经 housekeeper.employee_id 反查。
     */
    private Long requireHousekeeperId(Long employeeId) {
        if (employeeId == null) {
            throw new BaseException("未登录，请先登录");
        }
        Housekeeper housekeeper = housekeeperMapper.getByEmployeeId(employeeId);
        if (housekeeper == null) {
            throw new BaseException(MessageConstant.HOUSEKEEPER_NOT_FOUND);
        }
        return housekeeper.getId();
    }

    /**
     * 校验必填原因并做长度保护
     */
    private String requireReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new BaseException(MessageConstant.ORDER_REJECT_REASON_REQUIRED);
        }
        String trimmed = reason.trim();
        if (trimmed.length() > MAX_REASON_LENGTH) {
            throw new BaseException(MessageConstant.ORDER_REJECT_REASON_TOO_LONG);
        }
        return trimmed;
    }

    /**
     * 状态校验失败时给用户的统一提示。
     * 重新查一次订单拿当前状态 —— CAS 失败恰恰说明它已经不是我们刚读到的那个状态了。
     */
    private String stateChangedMessage(Long orderId) {
        Order latest = orderMapper.getById(orderId);
        return OrderStatusUtil.alreadyMessage(latest == null ? null : latest.getStatus());
    }

    /**
     * 解析预计到达时间，失败返回 null（由调用方统一报「请填写预计到达时间」）。
     * 兼容 "yyyy-MM-dd HH:mm"、"yyyy-MM-dd HH:mm:ss"、以及 ISO 的 T 分隔写法。
     */
    private LocalDateTime parseEstimatedArrival(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String s = text.trim().replace('T', ' ');
        for (DateTimeFormatter fmt : ARRIVAL_FORMATS) {
            try {
                return LocalDateTime.parse(s, fmt);
            } catch (Exception ignored) {
                // 换下一种格式继续试
            }
        }
        return null;
    }

    /**
     * Order → WorkerOrderVO，附带明细列表。
     * 有意不带金额与支付信息，避免把用户的支付情况暴露给家政员。
     */
    private WorkerOrderVO toVO(Order order) {
        WorkerOrderVO vo = new WorkerOrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setOrderDetailList(orderDetailMapper.listByOrderId(order.getId()));
        return vo;
    }
}
