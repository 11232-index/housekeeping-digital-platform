package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 员工端指派家政员传输对象
 */
@Data
public class OrderAssignDTO implements Serializable {

    /** 订单id */
    private Long orderId;

    /** 指派的家政员id */
    private Long housekeeperId;
}
