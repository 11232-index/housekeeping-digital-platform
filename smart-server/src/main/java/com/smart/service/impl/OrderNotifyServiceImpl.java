package com.smart.service.impl;

import com.smart.entity.OrderNotify;
import com.smart.mapper.OrderNotifyMapper;
import com.smart.service.OrderNotifyService;
import com.smart.vo.OrderNotifyVO;
import com.smart.websocket.OrderNotifyWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 订单提醒消息服务实现
 */
@Slf4j
@Service
public class OrderNotifyServiceImpl implements OrderNotifyService {

    @Autowired
    private OrderNotifyMapper orderNotifyMapper;

    @Override
    public void push(String type, String title, String content,
                     Long orderId, String orderNumber, BigDecimal amount) {
        try {
            OrderNotify notify = OrderNotify.builder()
                    .type(type)
                    .title(title)
                    .content(content)
                    .orderId(orderId)
                    .orderNumber(orderNumber)
                    .amount(amount)
                    .createTime(LocalDateTime.now())
                    .build();
            orderNotifyMapper.insert(notify);
            log.info("订单提醒消息已保存：id={}, type={}, orderNumber={}", notify.getId(), type, orderNumber);
        } catch (Exception e) {
            // 消息落库失败不影响主业务
            log.error("订单提醒消息落库失败：{}", e.getMessage());
        }
        // 广播给在线 Web 员工端（无人在线则仅记日志）
        OrderNotifyWebSocketServer.pushOrderNotify(type, title, content, orderId, orderNumber, amount);
    }

    @Override
    public List<OrderNotifyVO> listRecent(Long employeeId, int limit) {
        List<OrderNotify> list = orderNotifyMapper.listRecent(limit);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> readIds = new HashSet<>(orderNotifyMapper.listReadIds(employeeId));
        List<OrderNotifyVO> vos = new ArrayList<>(list.size());
        for (OrderNotify n : list) {
            vos.add(OrderNotifyVO.builder()
                    .id(n.getId())
                    .type(n.getType())
                    .title(n.getTitle())
                    .content(n.getContent())
                    .orderId(n.getOrderId())
                    .orderNumber(n.getOrderNumber())
                    .amount(n.getAmount())
                    .createTime(n.getCreateTime())
                    .read(readIds.contains(n.getId()))
                    .build());
        }
        return vos;
    }

    @Override
    public long countUnread(Long employeeId) {
        return orderNotifyMapper.countUnread(employeeId);
    }

    @Override
    public void markRead(Long employeeId, Long notifyId) {
        orderNotifyMapper.insertRead(employeeId, notifyId);
    }

    @Override
    public void markAllRead(Long employeeId) {
        orderNotifyMapper.markAllRead(employeeId);
    }
}
