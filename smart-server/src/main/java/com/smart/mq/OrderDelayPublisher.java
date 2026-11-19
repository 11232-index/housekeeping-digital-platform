package com.smart.mq;

import com.smart.config.RabbitMQConfiguration;
import com.smart.entity.Order;
import com.smart.event.OrderDelayMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单延迟消息发布器。
 * <p>
 * 只负责投递“到点提醒”，不做可靠消息表：RabbitMQ 不可用时发送失败只记日志，
 * 由 OrderTask 每分钟巡检兜底（状态判断全部是 CAS，不会重复执行）。
 */
@Slf4j
@Component
public class OrderDelayPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /** 15 分钟后检查是否仍未支付 */
    public void scheduleUnpaidCancel(Order order) {
        send(RabbitMQConfiguration.ROUTING_DELAY_15M,
                OrderDelayMessage.ACTION_UNPAID_CANCEL, order);
    }

    /** 5 分钟后检查接单情况（未确认提醒 / 临近服务无人接单） */
    public void scheduleAcceptCheck(Order order) {
        send(RabbitMQConfiguration.ROUTING_DELAY_5M,
                OrderDelayMessage.ACTION_ACCEPT_CHECK, order);
    }

    public void rescheduleAcceptCheck(Long orderId, String orderNumber) {
        OrderDelayMessage message = OrderDelayMessage.builder()
                .orderId(orderId)
                .orderNumber(orderNumber)
                .action(OrderDelayMessage.ACTION_ACCEPT_CHECK)
                .build();
        send(RabbitMQConfiguration.ROUTING_DELAY_5M, message);
    }

    private void send(String routingKey, String action, Order order) {
        if (order == null || order.getId() == null) {
            return;
        }
        send(routingKey, OrderDelayMessage.builder()
                .orderId(order.getId())
                .orderNumber(order.getNumber())
                .action(action)
                .build());
    }

    private void send(String routingKey, OrderDelayMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfiguration.EXCHANGE_ORDER_DELAY, routingKey, message);
            log.info("延迟消息已投递：routingKey={}, orderId={}, action={}",
                    routingKey, message.getOrderId(), message.getAction());
        } catch (Exception e) {
            log.warn("延迟消息投递失败（由 OrderTask 兜底）：orderId={}, action={}, error={}",
                    message.getOrderId(), message.getAction(), e.getMessage());
        }
    }
}
