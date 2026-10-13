package com.smart.service.impl;

import com.smart.entity.UserNotify;
import com.smart.mapper.UserNotifyMapper;
import com.smart.service.UserNotifyService;
import com.smart.vo.UserNotifyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户订单消息服务实现
 */
@Slf4j
@Service
public class UserNotifyServiceImpl implements UserNotifyService {

    @Autowired
    private UserNotifyMapper userNotifyMapper;

    @Override
    public void push(Long userId, String type, String title, String content, Long orderId, String orderNumber) {
        if (userId == null) {
            return;
        }
        try {
            UserNotify notify = UserNotify.builder()
                    .userId(userId)
                    .type(type)
                    .title(title)
                    .content(content)
                    .orderId(orderId)
                    .orderNumber(orderNumber)
                    .readFlag(UserNotify.READ_FLAG_UNREAD)
                    .createTime(LocalDateTime.now())
                    .build();
            userNotifyMapper.insert(notify);
            log.info("用户消息已保存：userId={}, type={}, orderNumber={}", userId, type, orderNumber);
        } catch (Exception e) {
            // 消息保存失败不影响主业务（与员工端 OrderNotifyServiceImpl.push 的处理保持一致）
            log.error("用户消息保存失败：userId={}, type={}, {}", userId, type, e.getMessage());
        }
    }

    @Override
    public List<UserNotifyVO> listRecent(Long userId, int limit) {
        List<UserNotify> list = userNotifyMapper.listRecent(userId, limit);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        List<UserNotifyVO> vos = new ArrayList<>(list.size());
        for (UserNotify n : list) {
            vos.add(UserNotifyVO.builder()
                    .id(n.getId())
                    .type(n.getType())
                    .title(n.getTitle())
                    .content(n.getContent())
                    .orderId(n.getOrderId())
                    .orderNumber(n.getOrderNumber())
                    .read(UserNotify.READ_FLAG_READ.equals(n.getReadFlag()))
                    .createTime(n.getCreateTime())
                    .build());
        }
        return vos;
    }

    @Override
    public long countUnread(Long userId) {
        return userNotifyMapper.countUnread(userId);
    }

    @Override
    public void markRead(Long userId, Long notifyId) {
        userNotifyMapper.markRead(userId, notifyId);
    }

    @Override
    public void markAllRead(Long userId) {
        userNotifyMapper.markAllRead(userId);
    }
}
