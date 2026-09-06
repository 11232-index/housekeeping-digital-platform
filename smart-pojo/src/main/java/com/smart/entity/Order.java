package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单状态：1待付款 */
    public static final Integer STATUS_PENDING_PAYMENT = 1;
    /** 订单状态：2待接单 */
    public static final Integer STATUS_PENDING_ACCEPT = 2;
    /** 订单状态：3已接单 */
    public static final Integer STATUS_ACCEPTED = 3;
    /** 订单状态：4服务中 */
    public static final Integer STATUS_IN_SERVICE = 4;
    /** 订单状态：5已完成 */
    public static final Integer STATUS_COMPLETED = 5;
    /** 订单状态：6退款中 */
    public static final Integer STATUS_REFUNDING = 6;
    /** 订单状态：7已取消 */
    public static final Integer STATUS_CANCELLED = 7;

    /** 支付方式：1微信支付 */
    public static final Integer PAY_METHOD_WECHAT = 1;
    /** 支付方式：2支付宝支付 */
    public static final Integer PAY_METHOD_ALIPAY = 2;

    /** 支付状态：0未支付 */
    public static final Integer PAY_STATUS_UNPAID = 0;
    /** 支付状态：1已支付 */
    public static final Integer PAY_STATUS_PAID = 1;
    /** 支付状态：2退款 */
    public static final Integer PAY_STATUS_REFUND = 2;

    /** 主键 */
    private Long id;

    /** 订单号 */
    private String number;

    /** 订单状态 */
    private Integer status;

    /** 用户id */
    private Long userId;

    /** 地址id */
    private Long addressBookId;

    /** 关联housekeeper_schedule表id */
    private Long housekeeperScheduleId;

    /** 预约日期 */
    private java.time.LocalDate scheduleDate;

    /** 预约时段（如：09:00~11:00） */
    private String scheduleTime;

    /** 服务时长（如：2小时、半天、全天） */
    private String serviceDuration;

    /** 下单时间 */
    private LocalDateTime orderTime;

    /** 付款时间 */
    private LocalDateTime payTime;

    /** 支付方式 */
    private Integer payMethod;

    /** 支付状态 */
    private Integer payStatus;

    /** 订单金额 */
    private BigDecimal amount;

    /** 备注信息 */
    private String remark;

    /** 手机号（冗余） */
    private String phone;

    /** 详细地址（冗余） */
    private String address;

    /** 联系人 */
    private String contactName;

    /** 取消原因 */
    private String cancelReason;

    /** 取消时间 */
    private LocalDateTime cancelTime;

    /** 预计上门时间 */
    private LocalDateTime estimatedServiceTime;

    /** 实际上门时间 */
    private LocalDateTime actualServiceTime;
}
