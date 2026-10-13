package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户订单消息（C端「消息中心」）
 * <p>
 * 与员工端的 OrderNotify 的区别：员工端一条消息要给多个员工看，已读状态放在独立的
 * order_notify_read 关系表里；用户消息是「一条只对一个用户」，所以已读标记就在本表上。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotify implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 是否已读：0未读 */
    public static final Integer READ_FLAG_UNREAD = 0;
    /** 是否已读：1已读 */
    public static final Integer READ_FLAG_READ = 1;

    private Long id;

    /** 接收用户id */
    private Long userId;

    /** 消息类型：accepted接单 rejected拒单 cannotServe无法服务 inService开始服务 completed已完成 cancelled已取消 refunded退款完成 timeout超时 */
    private String type;

    /** 提示标题 */
    private String title;

    /** 提示内容 */
    private String content;

    /** 关联订单id */
    private Long orderId;

    /** 订单号（冗余，便于直接展示） */
    private String orderNumber;

    /** 是否已读：0未读 1已读 */
    private Integer readFlag;

    /** 创建时间 */
    private LocalDateTime createTime;
}
