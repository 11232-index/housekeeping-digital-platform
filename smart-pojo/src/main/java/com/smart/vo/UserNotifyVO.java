package com.smart.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户订单消息视图对象（C端「消息中心」列表用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotifyVO implements Serializable {

    private Long id;

    /** 消息类型 */
    private String type;

    /** 提示标题 */
    private String title;

    /** 提示内容 */
    private String content;

    /** 关联订单id（点击可跳订单详情） */
    private Long orderId;

    /** 订单号（冗余） */
    private String orderNumber;

    /** 是否已读 */
    private Boolean read;

    /** 创建时间 */
    private LocalDateTime createTime;
}
