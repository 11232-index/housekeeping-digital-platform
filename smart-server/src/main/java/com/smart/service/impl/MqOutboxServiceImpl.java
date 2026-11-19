package com.smart.service.impl;

import com.smart.entity.MqOutbox;
import com.smart.mapper.MqOutboxMapper;
import com.smart.service.MqOutboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地消息表服务实现。
 */
@Slf4j
@Service
public class MqOutboxServiceImpl implements MqOutboxService {

    /** 超过该次数后不再自动重试，保留 status=2 供人工排查 */
    private static final int MAX_RETRY = 8;

    @Autowired
    private MqOutboxMapper mqOutboxMapper;

    @Override
    public void save(MqOutbox outbox) {
        LocalDateTime now = LocalDateTime.now();
        outbox.setStatus(MqOutbox.STATUS_PENDING);
        outbox.setRetryCount(0);
        outbox.setCreateTime(now);
        outbox.setUpdateTime(now);
        mqOutboxMapper.insert(outbox);
        log.info("写入本地消息表：eventId={}, type={}, orderId={}",
                outbox.getEventId(), outbox.getEventType(), outbox.getAggregateId());
    }

    @Override
    public List<MqOutbox> listPending(int limit) {
        return mqOutboxMapper.listPending(limit, MAX_RETRY);
    }

    @Override
    public void markSent(Long id) {
        mqOutboxMapper.markSent(id);
    }

    @Override
    public void markFailed(Long id, String errorMsg, LocalDateTime nextRetryTime) {
        String msg = errorMsg;
        if (msg != null && msg.length() > 500) {
            msg = msg.substring(0, 500);
        }
        mqOutboxMapper.markFailed(id, msg, nextRetryTime);
    }
}
