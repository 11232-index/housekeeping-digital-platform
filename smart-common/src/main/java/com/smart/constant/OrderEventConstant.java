package com.smart.constant;

/**
 * 订单事件类型（同时也是 RabbitMQ routing key）。
 */
public class OrderEventConstant {

    /** 下单成功 */
    public static final String CREATED = "order.created";

    /** 支付成功 */
    public static final String PAID = "order.paid";

    /** 员工指派家政员 */
    public static final String ASSIGNED = "order.assigned";

    /** 家政员接单 */
    public static final String ACCEPTED = "order.accepted";

    /** 家政员到达 / 开始服务 */
    public static final String ARRIVED = "order.arrived";

    /** 服务完成 */
    public static final String COMPLETED = "order.completed";

    /** 取消 / 申请退款（进入待退款） */
    public static final String CANCELLED = "order.cancelled";

    /** 退款完成 */
    public static final String REFUNDED = "order.refunded";

    private OrderEventConstant() {
    }
}
