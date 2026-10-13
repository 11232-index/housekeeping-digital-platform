package com.smart.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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

    /**
     * 该订单由哪位家政员接单（用户下单指定 或 员工端指派）。
     * 注意：它不用于区分订单类型 —— 区分「家政员订单/服务订单」仍然只看 order_detail.housekeeper_id。
     */
    private Long housekeeperId;

    /** 员工指派时刻（一级提醒的计时起点；用户指定订单为空） */
    private LocalDateTime assignTime;

    /** 一级提醒已推送时刻（幂等标记，防止定时任务每分钟重复推送） */
    private LocalDateTime remindTime;

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

    /** 订单金额（用了优惠券时这里就是折后的实付金额） */
    private BigDecimal amount;

    /**
     * 使用的优惠券id（没用券为 NULL）。
     * 同样是雪花id，必须序列化成字符串给前端（见 UserVoucherVO#voucherId 的说明）。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long voucherId;

    /** 优惠券标题（快照，订单详情直接展示，不用回查券表） */
    private String voucherTitle;

    /** 折扣率快照（80=八折） */
    private Long voucherDiscount;

    /** 优惠金额（原价 - 折后实付） */
    private BigDecimal discountAmount;

    /** 备注信息 */
    private String remark;

    /** 手机号（冗余） */
    private String phone;

    /** 详细地址（冗余） */
    private String address;

    /** 联系人 */
    private String contactName;

    /** 取消原因（记录「为什么变成了待退款(6)」：超时未接单/员工取消/家政员拒单/用户申请） */
    private String cancelReason;

    /** 退款原因（员工端在待退款(6) → 已取消(7) 时填写） */
    private String refundReason;

    /** 取消时间 */
    private LocalDateTime cancelTime;

    /** 预计上门时间 */
    private LocalDateTime estimatedServiceTime;

    /** 实际上门时间 */
    private LocalDateTime actualServiceTime;
}
