package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预约清单
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingList implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //名称
    private String name;

    //客户id
    private Long userId;

    //服务项目id
    private Long serviceItemId;

    //服务套餐id
    private Long servicePackageId;

    //服务要求
    private String requirement;

    //金额
    private BigDecimal amount;

    //图片
    private String image;

    private LocalDateTime createTime;
}
