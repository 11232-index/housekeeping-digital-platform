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

    /** 手机号（模糊搜索） */
    private String phone;

    /** 下单开始时间 */
    private String beginTime;

    /** 下单截止时间 */
    private String endTime;

    /**
     * 订单类型：service=服务订单（服务项目/服务套餐），housekeeper=家政员订单，为空查全部。
     * 订单表本身没有类型字段，依据 order_detail 上的 service_item_id/service_package_id/housekeeper_id 判断。
     */
    private String orderType;
}
