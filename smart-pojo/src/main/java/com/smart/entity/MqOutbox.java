package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * RabbitMQ 本地消息表。
 * <p>
 * 业务事务内插入，定时任务扫描后发送到 MQ；
 * 发送成功标记 status=1，失败按退避时间重试，避免业务回滚了消息却已经发出去。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MqOutbox implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_SENT = 1;
    public static final int STATUS_FAILED = 2;

    private Long id;

    /** 事件唯一ID（消费端幂等用） */
    private String eventId;

    /** 事件类型，如 order.created */
    private String eventType;

    /** 发送时使用的 routing key */
    private String routingKey;

    /** 聚合类型，目前固定 order */
    private String aggregateType;

    /** 聚合根id，目前是订单id */
    private Long aggregateId;

    /** 消息体 JSON */
    private String payload;

    /** 0待发送 1已发送 2发送失败 */
    private Integer status;

    /** 已重试次数 */
    private Integer retryCount;

    /** 下次重试时间 */
    private LocalDateTime nextRetryTime;

    /** 最近一次失败原因 */
    private String errorMsg;

    /** 发送成功时间 */
    private LocalDateTime sentTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
