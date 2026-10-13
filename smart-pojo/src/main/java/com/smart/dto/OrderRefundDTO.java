package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 员工端确认退款传输对象（待退款 6 → 已取消 7）
 */
@Data
public class OrderRefundDTO implements Serializable {

    /** 订单id */
    private Long orderId;

    /** 退款原因 */
    private String refundReason;
}
