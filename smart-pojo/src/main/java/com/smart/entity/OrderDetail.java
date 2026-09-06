package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细表实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 订单id */
    private Long orderId;

    /** 家政员id */
    private Long housekeeperId;

    /** 服务项目id */
    private Long serviceItemId;

    /** 服务套餐id */
    private Long servicePackageId;

    /** 名称（家政员姓名/服务名/套餐名） */
    private String name;

    /** 图片 */
    private String image;

    /** 服务要求（如：需自带工具、周末服务） */
    private String requirement;

    /** 金额 */
    private BigDecimal amount;
}
