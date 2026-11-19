package com.smart.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 订单状态事件，发送到 RabbitMQ 的 order.events。
 * <p>
 * 消费端只把它当作“订单变了”的通知，真正的状态以数据库为准，
 * 因此消息里带的是变更时的快照，消费前必须重新查库校验。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 事件唯一ID，消费端用于幂等 */
    private String eventId;

    /** 事件类型，同时也是 routing key，如 order.created */
    private String eventType;

    private Long orderId;

    private String orderNumber;

    private Long userId;

    /** 家政员id（指派后才有） */
    private Long housekeeperId;

    /** 操作人员id（员工端操作时写入） */
    private Long employeeId;

    /** 操作方：user / employee / worker，用于区分谁触发了状态变更 */
    private String operator;

    private BigDecimal amount;

    /** 变更后的订单状态 */
    private Integer status;

    /** 变更后的支付状态 */
    private Integer payStatus;

    /** 取消/退款/拒单等原因 */
    private String reason;

    /** 接单时的预计到达时间 */
    private LocalDateTime estimatedServiceTime;

    /** 事件发生时间 */
    private LocalDateTime occurredAt;

    /** 预留扩展字段 */
    private Map<String, Object> extra;
}
