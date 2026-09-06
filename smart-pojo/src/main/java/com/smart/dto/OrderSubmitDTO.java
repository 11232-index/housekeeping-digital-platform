package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户提交订单传输对象
 */
@Data
public class OrderSubmitDTO implements Serializable {

    /** 地址id */
    private Long addressBookId;

    /** 联系电话 */
    private String phone;

    /** 详细地址（冗余） */
    private String address;

    /** 联系人 */
    private String contactName;

    /** 备注 */
    private String remark;

    /** 支付方式 1微信支付 2支付宝支付 */
    private Integer payMethod;

    /** 关联housekeeper_schedule表id（预约家政员时传入） */
    private Long housekeeperScheduleId;

    /** 预约日期 */
    private String scheduleDate;

    /** 预约时段（如：8:00~9:00） */
    private String scheduleTime;

    /** 服务时长（如：2小时、半天、全天） */
    private String serviceDuration;

    /** 单条明细（提交单项时传入） */
    private OrderItemDTO item;
}
