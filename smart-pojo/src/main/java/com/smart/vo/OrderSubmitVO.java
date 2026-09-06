package com.smart.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单提交成功返回视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSubmitVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单id */
    private Long orderId;

    /** 订单号 */
    private String orderNumber;

    /** 订单金额 */
    private BigDecimal amount;

    /** 下单时间 */
    private LocalDateTime orderTime;
}
