package com.smart.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单提醒消息 VO（含当前员工是否已读）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderNotifyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String type;

    private String title;

    private String content;

    private Long orderId;

    private String orderNumber;

    private BigDecimal amount;

    private LocalDateTime createTime;

    /** 当前登录员工是否已读 */
    private Boolean read;
}
