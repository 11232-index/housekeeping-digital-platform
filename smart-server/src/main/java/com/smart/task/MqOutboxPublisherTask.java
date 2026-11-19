package com.smart.task;

import com.alibaba.fastjson.JSON;
import com.smart.config.RabbitMQConfiguration;
import com.smart.entity.MqOutbox;
import com.smart.event.OrderEvent;
import com.smart.service.MqOutboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地消息表发送任务（Spring Task 兜底）。
 * <p>
 * 订单事件先写 mq_outbox，本任务每 10 秒扫描一次：
 * <ul>
 *   <li>发送成功 → status=1</li>
 *   <li>发送失败 → status=2，按退避时间重试，最多 8 次</li>
 * </ul>
 * RabbitMQ 宕机期间消息会积压在表里，恢复后自动补发。
 */
@Slf4j
@Component
public class MqOutboxPublisherTask {

    private static final int BATCH_SIZE = 100;

    private static final int MAX_BACKOFF_SECONDS = 300;

    @Autowired
    private MqOutboxService mqOutboxService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 3_000, initialDelay = 5_000)
    public void publishPendingEvents() {
        List<MqOutbox> pending = mqOutboxService.listPending(BATCH_SIZE);
        if (pending == null || pending.isEmpty()) {
            return;
        }

        for (MqOutbox outbox : pending) {
            try {
                OrderEvent event = JSON.parseObject(outbox.getPayload(), OrderEvent.class);
                rabbitTemplate.convertAndSend(
                        RabbitMQConfiguration.EXCHANGE_ORDER_EVENTS,
                        outbox.getRoutingKey(),
                        event);
                mqOutboxService.markSent(outbox.getId());
                log.info("订单事件已发送：eventId={}, type={}, orderId={}",
                        outbox.getEventId(), outbox.getEventType(), outbox.getAggregateId());
            } catch (Exception e) {
                int retry = outbox.getRetryCount() == null ? 0 : outbox.getRetryCount();
                long delay = Math.min(MAX_BACKOFF_SECONDS, 10L * (1L << Math.min(retry, 5)));
                LocalDateTime next = LocalDateTime.now().plusSeconds(delay);
                mqOutboxService.markFailed(outbox.getId(), e.getMessage(), next);
                log.warn("订单事件发送失败，{} 秒后重试：eventId={}, type={}, error={}",
                        delay, outbox.getEventId(), outbox.getEventType(), e.getMessage());
            }
        }
    }
}
