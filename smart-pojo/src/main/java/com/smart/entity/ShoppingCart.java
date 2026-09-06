package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCart implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //服务名称
    private String name;

    //服务图片路径
    private String image;

    //用户id
    private Long userId;

    //服务项目id
    private Long serviceItemId;

    //服务套餐id
    private Long servicePackageId;

    //服务要求
    private String requirement;

    //服务单价
    private BigDecimal amount;

    //创建时间
    private LocalDateTime createTime;
}
