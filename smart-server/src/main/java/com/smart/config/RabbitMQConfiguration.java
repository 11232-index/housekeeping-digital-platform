package com.smart.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 拓扑配置。
 * <pre>
 * order.events (topic)
 *   ├─ order.notify.queue  → 通知中心（写 notify 表 + WebSocket 定向推送）
 *   ├─ order.stats.queue   → 统计/报表
 *   └─ order.cache.queue   → 缓存刷新（预留）
 *
 * 消费失败统一进入 order.dlx → order.dlq
 * </pre>
 */
@Configuration
public class RabbitMQConfiguration {

    public static final String EXCHANGE_ORDER_EVENTS = "order.events";
    public static final String EXCHANGE_ORDER_DLX = "order.dlx";

    public static final String QUEUE_ORDER_NOTIFY = "order.notify.queue";
    public static final String QUEUE_ORDER_STATS = "order.stats.queue";
    public static final String QUEUE_ORDER_CACHE = "order.cache.queue";
    public static final String QUEUE_ORDER_DLQ = "order.dlq";

    public static final String ROUTING_DLQ = "order.dead";

    // ===== 延迟消息（TTL + 死信，不依赖 delayed-message 插件）=====
    public static final String EXCHANGE_ORDER_DELAY = "order.delay.exchange";
    public static final String EXCHANGE_ORDER_DELAY_PROCESS = "order.delay.process.exchange";

    /** 15 分钟后处理：未支付自动取消 */
    public static final String QUEUE_DELAY_15M = "order.delay.15m.queue";
    /** 每 5 分钟检查一次：接单提醒 / 临近服务无人接单 */
    public static final String QUEUE_DELAY_5M = "order.delay.5m.queue";
    /** 延迟消息到期后统一进入这个队列消费 */
    public static final String QUEUE_DELAY_PROCESS = "order.delay.process.queue";

    public static final String ROUTING_DELAY_15M = "delay.15m";
    public static final String ROUTING_DELAY_5M = "delay.5m";
    public static final String ROUTING_UNPAID_CANCEL = "unpaid_cancel";
    public static final String ROUTING_ACCEPT_CHECK = "accept_check";

    /** 所有订单事件统一用 order.* 绑定 */
    public static final String ORDER_EVENT_ROUTING_PATTERN = "order.*";

    @Bean
    public TopicExchange orderEventsExchange() {
        return new TopicExchange(EXCHANGE_ORDER_EVENTS, true, false);
    }

    @Bean
    public DirectExchange orderDlxExchange() {
        return new DirectExchange(EXCHANGE_ORDER_DLX, true, false);
    }

    @Bean
    public Queue orderNotifyQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_NOTIFY)
                .deadLetterExchange(EXCHANGE_ORDER_DLX)
                .deadLetterRoutingKey(ROUTING_DLQ)
                .build();
    }

    @Bean
    public Queue orderStatsQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_STATS)
                .deadLetterExchange(EXCHANGE_ORDER_DLX)
                .deadLetterRoutingKey(ROUTING_DLQ)
                .build();
    }

    @Bean
    public Queue orderCacheQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_CACHE)
                .deadLetterExchange(EXCHANGE_ORDER_DLX)
                .deadLetterRoutingKey(ROUTING_DLQ)
                .build();
    }

    @Bean
    public Queue orderDlqQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_DLQ).build();
    }

    @Bean
    public Binding orderDlqBinding() {
        return BindingBuilder.bind(orderDlqQueue())
                .to(orderDlxExchange())
                .with(ROUTING_DLQ);
    }

    @Bean
    public Binding orderNotifyBinding() {
        return BindingBuilder.bind(orderNotifyQueue())
                .to(orderEventsExchange())
                .with(ORDER_EVENT_ROUTING_PATTERN);
    }

    @Bean
    public Binding orderStatsBinding() {
        return BindingBuilder.bind(orderStatsQueue())
                .to(orderEventsExchange())
                .with(ORDER_EVENT_ROUTING_PATTERN);
    }

    @Bean
    public Binding orderCacheBinding() {
        return BindingBuilder.bind(orderCacheQueue())
                .to(orderEventsExchange())
                .with(ORDER_EVENT_ROUTING_PATTERN);
    }

    // ===== 延迟消息拓扑 =====

    @Bean
    public DirectExchange orderDelayExchange() {
        return new DirectExchange(EXCHANGE_ORDER_DELAY, true, false);
    }

    @Bean
    public DirectExchange orderDelayProcessExchange() {
        return new DirectExchange(EXCHANGE_ORDER_DELAY_PROCESS, true, false);
    }

    @Bean
    public Queue orderDelay15mQueue() {
        return QueueBuilder.durable(QUEUE_DELAY_15M)
                .ttl(15 * 60 * 1000)
                .deadLetterExchange(EXCHANGE_ORDER_DELAY_PROCESS)
                .deadLetterRoutingKey(ROUTING_UNPAID_CANCEL)
                .build();
    }

    @Bean
    public Queue orderDelay5mQueue() {
        return QueueBuilder.durable(QUEUE_DELAY_5M)
                .ttl(5 * 60 * 1000)
                .deadLetterExchange(EXCHANGE_ORDER_DELAY_PROCESS)
                .deadLetterRoutingKey(ROUTING_ACCEPT_CHECK)
                .build();
    }

    @Bean
    public Queue orderDelayProcessQueue() {
        return QueueBuilder.durable(QUEUE_DELAY_PROCESS)
                .deadLetterExchange(EXCHANGE_ORDER_DLX)
                .deadLetterRoutingKey(ROUTING_DLQ)
                .build();
    }

    @Bean
    public Binding orderDelay15mBinding() {
        return BindingBuilder.bind(orderDelay15mQueue())
                .to(orderDelayExchange())
                .with(ROUTING_DELAY_15M);
    }

    @Bean
    public Binding orderDelay5mBinding() {
        return BindingBuilder.bind(orderDelay5mQueue())
                .to(orderDelayExchange())
                .with(ROUTING_DELAY_5M);
    }

    @Bean
    public Binding orderDelayProcessUnpaidBinding() {
        return BindingBuilder.bind(orderDelayProcessQueue())
                .to(orderDelayProcessExchange())
                .with(ROUTING_UNPAID_CANCEL);
    }

    @Bean
    public Binding orderDelayProcessAcceptBinding() {
        return BindingBuilder.bind(orderDelayProcessQueue())
                .to(orderDelayProcessExchange())
                .with(ROUTING_ACCEPT_CHECK);
    }

    /**
     * 消息体统一用 JSON，时间格式交给 JavaTimeModule。
     */
    @Bean
    public Jackson2JsonMessageConverter rabbitMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }
}
