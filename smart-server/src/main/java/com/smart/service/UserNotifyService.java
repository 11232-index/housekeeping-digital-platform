package com.smart.service;

import com.smart.vo.UserNotifyVO;

import java.util.List;

/**
 * 用户订单消息服务接口（C端「消息中心」）
 */
public interface UserNotifyService {

    /**
     * 给用户发一条订单消息。
     * <p>
     * 刻意不抛异常：通知是附属动作，发失败不能影响下单/接单这些主流程。
     *
     * @param userId      接收用户id
     * @param type        消息类型（accepted/rejected/cannotServe/inService/completed/cancelled/refunded/timeout）
     * @param title       标题
     * @param content     内容
     * @param orderId     关联订单id
     * @param orderNumber 订单号
     */
    void push(Long userId, String type, String title, String content, Long orderId, String orderNumber);

    /**
     * 某用户最近 N 条消息
     */
    List<UserNotifyVO> listRecent(Long userId, int limit);

    /**
     * 某用户的未读消息数
     */
    long countUnread(Long userId);

    /**
     * 标记单条已读
     */
    void markRead(Long userId, Long notifyId);

    /**
     * 全部已读
     */
    void markAllRead(Long userId);
}
