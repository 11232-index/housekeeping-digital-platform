package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 购物车数据传输对象
 */
@Data
public class ShoppingCartDTO implements Serializable {

    //服务项目id
    private Long serviceItemId;

    //服务套餐id
    private Long servicePackageId;

    //服务要求
    private String requirement;
}
