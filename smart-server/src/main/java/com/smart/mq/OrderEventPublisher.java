package com.smart.mq;

import com.alibaba.fastjson.JSON;
import com.smart.entity.MqOutbox;
import com.smart.entity.Order;
import com.smart.event.OrderEvent;
import com.smart.service.MqOutboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 订单事件发布器。
 * <p>
 * 不直接发 MQ：只把事件写进本地消息表 mq_outbox，
 * 由 {@code MqOutboxPublisherTask} 在事务提交后再发送。
 * 这样订单事务回滚时消息不会提前发出去，MQ 宕机时也不会丢事件。
 */
@Slf4j
@Component
public class OrderEventPublisher {

    @Autowired
    private MqOutboxService mqOutboxService;

    public void publish(String eventType, Order order) {
        publish(eventType, order, null, null, null);
    }

    public void publish(String eventType, Order order, String reason) {
        publish(eventType, order, null, reason, null);
    }

    public void publish(String eventType, Order order, Long employeeId,
                        String reason, LocalDateTime estimatedServiceTime) {
        publish(eventType, order, employeeId, reason, estimatedServiceTime, null);
    }

    public void publish(String eventType, Order order, Long employeeId,
                        String reason, LocalDateTime estimatedServiceTime, String operator) {
        if (order == null || order.getId() == null) {
            log.warn("订单事件缺少订单id，跳过：type={}", eventType);
            return;
        }

        OrderEvent event = OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .orderId(order.getId())
                .orderNumber(order.getNumber())
                .userId(order.getUserId())
                .housekeeperId(order.getHousekeeperId())
                .employeeId(employeeId)
                .operator(operator)
                .amount(order.getAmount())
                .status(order.getStatus())
                .payStatus(order.getPayStatus())
                .reason(reason)
                .estimatedServiceTime(estimatedServiceTime)
                .occurredAt(LocalDateTime.now())
                .build();

        MqOutbox outbox = MqOutbox.builder()
                .eventId(event.getEventId())
                .eventType(eventType)
                .routingKey(eventType)
                .aggregateType("order")
                .aggregateId(order.getId())
                .payload(JSON.toJSONString(event))
                .build();

        mqOutboxService.save(outbox);
    }
}
