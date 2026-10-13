package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单提醒消息（来单提醒 / 客户催单）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderNotify implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 消息类型：newOrder 来单提醒；urge 客户催单 */
    private String type;

    /** 提示标题 */
    private String title;

    /** 提示/播报内容 */
    private String content;

    /** 关联订单id */
    private Long orderId;

    /** 订单号（冗余） */
    private String orderNumber;

    /** 订单金额 */
    private BigDecimal amount;

    /** 创建时间 */
    private LocalDateTime createTime;
}
