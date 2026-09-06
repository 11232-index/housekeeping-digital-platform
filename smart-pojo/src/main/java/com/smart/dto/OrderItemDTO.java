package com.smart.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细项（前端传）
 */
@Data
public class OrderItemDTO implements Serializable {

    /** 服务项目id */
    private Long serviceItemId;

    /** 服务套餐id */
    private Long servicePackageId;

    /** 名称 */
    private String name;

    /** 图片 */
    private String image;

    /** 服务要求 */
    private String requirement;

    /** 家政员id */
    private Long housekeeperId;

    /** 金额 */
    private BigDecimal amount;
}
