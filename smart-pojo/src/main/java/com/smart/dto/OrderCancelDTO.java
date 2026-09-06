package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 取消订单传输对象
 */
@Data
public class OrderCancelDTO implements Serializable {

    /** 订单id */
    private Long orderId;

    /** 取消原因 */
    private String cancelReason;
}
