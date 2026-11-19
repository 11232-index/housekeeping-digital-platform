package com.smart.mapper;

import com.smart.entity.MqOutbox;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RabbitMQ 本地消息表。
 */
@Mapper
public interface MqOutboxMapper {

    @Insert("INSERT INTO mq_outbox (event_id, event_type, routing_key, aggregate_type, aggregate_id, payload, " +
            "status, retry_count, next_retry_time, create_time, update_time) " +
            "VALUES (#{eventId}, #{eventType}, #{routingKey}, #{aggregateType}, #{aggregateId}, #{payload}, " +
            "#{status}, #{retryCount}, #{nextRetryTime}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(MqOutbox outbox);

    /**
     * 待发送 / 到点重试的消息。
     * 失败超过 maxRetry 次的记录保留为 status=2，等待人工或后续对账处理。
     */
    @Select("SELECT * FROM mq_outbox " +
            "WHERE status = 0 " +
            "   OR (status = 2 AND retry_count < #{maxRetry} AND next_retry_time <= NOW()) " +
            "ORDER BY id LIMIT #{limit}")
    List<MqOutbox> listPending(@Param("limit") int limit, @Param("maxRetry") int maxRetry);

    @Update("UPDATE mq_outbox SET status = 1, sent_time = NOW(), update_time = NOW(), error_msg = NULL " +
            "WHERE id = #{id}")
    int markSent(@Param("id") Long id);

    @Update("UPDATE mq_outbox SET status = 2, retry_count = retry_count + 1, error_msg = #{errorMsg}, " +
            "next_retry_time = #{nextRetryTime}, update_time = NOW() WHERE id = #{id}")
    int markFailed(@Param("id") Long id,
                   @Param("errorMsg") String errorMsg,
                   @Param("nextRetryTime") LocalDateTime nextRetryTime);
}
