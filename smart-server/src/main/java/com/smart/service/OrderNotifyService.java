package com.smart.service;

import com.smart.vo.OrderNotifyVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单提醒消息服务（来单提醒 / 客户催单）
 */
public interface OrderNotifyService {

    /**
     * 产生一条提醒：先落库（order_notify），再广播给在线 Web 员工端；
     * 保存/广播失败不影响主业务（内部 try-catch）
     */
    void push(String type, String title, String content, Long orderId, String orderNumber, BigDecimal amount);

    /**
     * 某员工最近 N 条消息（含该员工是否已读）
     */
    List<OrderNotifyVO> listRecent(Long employeeId, int limit);

    /**
     * 某员工未读消息数
     */
    long countUnread(Long employeeId);

    /**
     * 标记某员工已读某条消息
     */
    void markRead(Long employeeId, Long notifyId);

    /**
     * 某员工一键全部已读
     */
    void markAllRead(Long employeeId);
}
