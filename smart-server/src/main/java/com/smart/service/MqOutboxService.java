package com.smart.service;

import com.smart.entity.MqOutbox;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地消息表服务。
 */
public interface MqOutboxService {

    /**
     * 保存一条待发送事件。必须在业务事务内调用，保证和业务数据同时提交。
     */
    void save(MqOutbox outbox);

    /**
     * 查询待发送或到点重试的消息。
     */
    List<MqOutbox> listPending(int limit);

    void markSent(Long id);

    void markFailed(Long id, String errorMsg, LocalDateTime nextRetryTime);
}
