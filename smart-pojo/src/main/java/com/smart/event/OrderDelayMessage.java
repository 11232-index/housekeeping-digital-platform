package com.smart.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 延迟消息体。
 * <p>
 * 消息到点后只作为“该检查这个订单了”的触发信号，
 * 真正的状态判断和 CAS 更新在消费者里重新查库完成。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDelayMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ACTION_UNPAID_CANCEL = "unpaid_cancel";
    public static final String ACTION_ACCEPT_CHECK = "accept_check";

    private Long orderId;

    /** unpaid_cancel / accept_check */
    private String action;

    private String orderNumber;
}
