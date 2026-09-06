package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单分页查询传输对象
 */
@Data
public class OrderPageQueryDTO implements Serializable {

    /** 当前页码 */
    private Integer page = 1;

    /** 每页条数 */
    private Integer pageSize = 10;

    /** 订单号（模糊搜索） */
    private String number;

    /** 订单状态 */
    private Integer status;

    /** 用户id */
    private Long userId;

    /** 联系人（模糊搜索） */
    private String contactName;

    /** 下单开始时间 */
    private String beginTime;

    /** 下单截止时间 */
    private String endTime;
}
