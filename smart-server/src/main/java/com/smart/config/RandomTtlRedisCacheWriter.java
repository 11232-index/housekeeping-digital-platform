package com.smart.config;

import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.CacheStatisticsCollector;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 自定义 Redis 缓存写入器 —— 给每次写入的 TTL 增加随机抖动，防止缓存雪崩
 * <p>
 * 背景：若同一批缓存 key 在同一时刻写入（缓存清空后集体回源、应用刚启动等），
 * 且 TTL 都是固定值，那么它们会在同一时刻集体过期，瞬时请求全部穿透到数据库（缓存雪崩）。
 * <p>
 * 做法：写入时在配置 TTL 的基础上随机增加 0 ~ TTL*JITTER_RATIO 的毫秒数，
 * 让每个 key 的过期时间错开（例如 30 分钟 TTL 实际落在 30分00秒 ~ 36分00秒之间），
 * 即使同批写入也不会同时过期。
 * <p>
 * 采用装饰器模式包装 {@link RedisCacheWriter#nonLockingRedisCacheWriter}：
 * 只覆写 put / putIfAbsent 两个写入方法，其它读写、清空、统计操作原样透传给被包装的 writer。
 */
public class RandomTtlRedisCacheWriter implements RedisCacheWriter {

    /** 随机抖动比例：每个 key 的 TTL 在配置值基础上随机增加 [0, TTL*JITTER_RATIO) 毫秒 */
    private static final double JITTER_RATIO = 0.2;

    private final RedisCacheWriter delegate;

    public RandomTtlRedisCacheWriter(RedisConnectionFactory connectionFactory) {
        this(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory));
    }

    private RandomTtlRedisCacheWriter(RedisCacheWriter delegate) {
        this.delegate = delegate;
    }

    @Override
    public RedisCacheWriter withStatisticsCollector(CacheStatisticsCollector cacheStatisticsCollector) {
        // 保留随机 TTL 能力，同时把统计收集透传给被包装的 writer
        return new RandomTtlRedisCacheWriter(delegate.withStatisticsCollector(cacheStatisticsCollector));
    }

    @Override
    public void put(String name, byte[] key, byte[] value, Duration ttl) {
        delegate.put(name, key, value, randomizeTtl(ttl));
    }

    @Override
    public byte[] putIfAbsent(String name, byte[] key, byte[] value, Duration ttl) {
        return delegate.putIfAbsent(name, key, value, randomizeTtl(ttl));
    }

    @Override
    public byte[] get(String name, byte[] key) {
        return delegate.get(name, key);
    }

    @Override
    public void remove(String name, byte[] key) {
        delegate.remove(name, key);
    }

    @Override
    public void clean(String name, byte[] pattern) {
        delegate.clean(name, pattern);
    }

    @Override
    public void clearStatistics(String cacheName) {
        delegate.clearStatistics(cacheName);
    }

    @Override
    public CacheStatistics getCacheStatistics(String cacheName) {
        return delegate.getCacheStatistics(cacheName);
    }

    /**
     * 给 TTL 增加随机抖动：[0, TTL*JITTER_RATIO) 毫秒；对 null / 0 / 负数 TTL 不做处理
     */
    private Duration randomizeTtl(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            return ttl;
        }
        long baseMillis = ttl.toMillis();
        long extraMillis = ThreadLocalRandom.current().nextLong(0, (long) (baseMillis * JITTER_RATIO) + 1);
        return ttl.plusMillis(extraMillis);
    }
}
