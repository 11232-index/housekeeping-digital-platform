package com.smart.mq;

import com.rabbitmq.client.Channel;
import com.smart.config.RabbitMQConfiguration;
import com.smart.entity.Order;
import com.smart.event.OrderDelayMessage;
import com.smart.mapper.OrderMapper;
import com.smart.service.HousekeeperScheduleService;
import com.smart.service.OrderNotifyService;
import com.smart.service.UserNotifyService;
import com.smart.utils.ScheduleTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 订单延迟消息消费者。
 * <p>
 * 到期后不做“拿到消息就执行”，而是重新查订单、判断当前状态后再 CAS 更新；
 * 因此重复投递、订单已支付/已取消等情况都只会被安全忽略。
 * 原有 OrderTask 继续每分钟巡检，作为 MQ 不可用时的兜底。
 */
@Slf4j
@Component
public class OrderDelayConsumer {

    private static final int UNPAID_TIMEOUT_MINUTES = 15;
    private static final String CANCEL_REASON_TIMEOUT = "订单超时";
    private static final String CANCEL_REASON_ACCEPT_TIMEOUT = "超时未接单";

    @Value("${smart.order.accept-remind-minutes:30}")
    private int acceptRemindMinutes;

    @Value("${smart.order.accept-timeout-hours:2}")
    private int acceptTimeoutHours;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private HousekeeperScheduleService housekeeperScheduleService;

    @Autowired
    private OrderNotifyService orderNotifyService;

    @Autowired
    private UserNotifyService userNotifyService;

    @Autowired
    private OrderDelayPublisher orderDelayPublisher;

    @Autowired
    private CacheManager cacheManager;

    @RabbitListener(queues = RabbitMQConfiguration.QUEUE_DELAY_PROCESS)
    public void onDelayMessage(OrderDelayMessage message,
                               Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            if (message == null || message.getOrderId() == null) {
                channel.basicAck(deliveryTag, false);
                return;
            }
            if (OrderDelayMessage.ACTION_UNPAID_CANCEL.equals(message.getAction())) {
                handleUnpaidCancel(message.getOrderId());
            } else if (OrderDelayMessage.ACTION_ACCEPT_CHECK.equals(message.getAction())) {
                handleAcceptCheck(message.getOrderId());
            } else {
                log.warn("未知延迟消息动作：{}", message.getAction());
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理延迟消息失败：orderId={}, action={}, error={}",
                    message == null ? null : message.getOrderId(),
                    message == null ? null : message.getAction(), e.getMessage(), e);
            // 不重新入队，交给死信队列 + OrderTask 兜底
            channel.basicNack(deliveryTag, false, false);
        }
    }

    /**
     * 15 分钟未支付：CAS 取消，释放时段，通知用户。
     */
    private void handleUnpaidCancel(Long orderId) {
        Order order = orderMapper.getById(orderId);
        if (order == null) {
            return;
        }
        if (!Order.STATUS_PENDING_PAYMENT.equals(order.getStatus())
                || !Order.PAY_STATUS_UNPAID.equals(order.getPayStatus())) {
            return;
        }
        LocalDateTime deadline = order.getOrderTime() == null
                ? null : order.getOrderTime().plusMinutes(UNPAID_TIMEOUT_MINUTES);
        if (deadline != null && deadline.isAfter(LocalDateTime.now())) {
            // 投递早了（理论上不会发生），重新投一次
            orderDelayPublisher.scheduleUnpaidCancel(order);
            return;
        }
        int rows = orderMapper.autoCancelTimeoutById(
                orderId, Order.STATUS_PENDING_PAYMENT, Order.STATUS_CANCELLED, CANCEL_REASON_TIMEOUT);
        if (rows == 0) {
            return;
        }
        if (order.getHousekeeperScheduleId() != null) {
            housekeeperScheduleService.tryRelease(order.getHousekeeperScheduleId());
        }
        evictOrderCache(orderId);
        userNotifyService.push(order.getUserId(), "cancelled", "订单已取消",
                "您的订单 " + order.getNumber() + " 因超时未支付已自动取消",
                orderId, order.getNumber());
        log.info("延迟消息：超时未支付订单已取消，id={}", orderId);
    }

    /**
     * 待接单检查：30 分钟未确认提醒；距服务开始不足 2 小时转待退款。
     * 条件未到则重新投递 5 分钟延时消息，继续检查。
     */
    private void handleAcceptCheck(Long orderId) {
        Order order = orderMapper.getById(orderId);
        if (order == null || !Order.STATUS_PENDING_ACCEPT.equals(order.getStatus())) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();

        // 一级提醒：指派后超过 N 分钟仍未确认
        if (order.getRemindTime() == null
                && order.getAssignTime() != null
                && order.getAssignTime().plusMinutes(acceptRemindMinutes).isBefore(now)) {
            orderMapper.markReminded(orderId);
            orderNotifyService.push("acceptRemind", "家政员未确认接单",
                    "订单 " + order.getNumber() + " 指派后 " + acceptRemindMinutes + " 分钟仍未确认，请电话跟进",
                    orderId, order.getNumber(), order.getAmount());
            log.info("延迟消息：已提醒员工跟进未接单订单，id={}", orderId);
        }

        // 二级超时：距服务开始不足 N 小时仍无人接单
        LocalDateTime serviceStart = ScheduleTimeUtil.parseStart(
                order.getScheduleDate(), order.getScheduleTime());
        if (serviceStart == null) {
            log.warn("延迟消息：订单时段无法解析，停止检查，id={}", orderId);
            return;
        }
        if (!serviceStart.minusHours(acceptTimeoutHours).isAfter(now)) {
            int rows = orderMapper.timeoutToRefundCas(
                    orderId, Order.STATUS_PENDING_ACCEPT, Order.STATUS_REFUNDING, CANCEL_REASON_ACCEPT_TIMEOUT);
            if (rows > 0) {
                evictOrderCache(orderId);
                orderNotifyService.push("refund", "订单超时待退款",
                        "订单 " + order.getNumber() + " 距服务开始不足 " + acceptTimeoutHours
                                + " 小时仍无人接单，已转入待退款",
                        orderId, order.getNumber(), order.getAmount());
                userNotifyService.push(order.getUserId(), "timeout", "订单超时",
                        "您的订单 " + order.getNumber() + " 因无人接单已转入退款处理",
                        orderId, order.getNumber());
                log.info("延迟消息：无人接单订单转待退款，id={}", orderId);
            }
            return;
        }

        // 还没到时间，5 分钟后再检查一次
        orderDelayPublisher.rescheduleAcceptCheck(orderId, order.getNumber());
    }

    private void evictOrderCache(Long orderId) {
        try {
            Cache cache = cacheManager.getCache("order");
            if (cache != null) {
                cache.evict("one:" + orderId);
            }
        } catch (Exception e) {
            log.error("清除订单缓存失败：id={}, {}", orderId, e.getMessage());
        }
    }
}
