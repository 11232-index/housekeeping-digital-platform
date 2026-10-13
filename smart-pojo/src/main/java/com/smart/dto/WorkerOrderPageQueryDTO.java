package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 家政员端「我的订单」分页查询传输对象
 */
@Data
public class WorkerOrderPageQueryDTO implements Serializable {

    /** 当前页码 */
    private Integer page = 1;

    /** 每页条数 */
    private Integer pageSize = 10;

    /** 订单状态（为空查全部） */
    private Integer status;
}
