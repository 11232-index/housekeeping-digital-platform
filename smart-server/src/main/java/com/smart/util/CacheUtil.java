package com.smart.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 缓存工具类 — 用于配合 @CacheEvict 精准删除分页缓存
 * <p>
 * 使用方式：在 update/delete 方法中调用 {@code cacheUtil.evictPageKeys("cacheName")}
 * 会删除 Redis 中所有 "cacheName::page:*" 前缀的 key。
 * 配合注解 {@code @CacheEvict(key = "'one:' + #id")} 精准删单条缓存，
 * 两者结合实现：单条缓存精准失效 + 分页缓存按前缀批量失效。
 */
@Component
@Slf4j
public class CacheUtil {

    /** Redis key 分隔符（Spring Cache 默认用 "::" 分隔 cache name 和 key） */
    private static final String SEPARATOR = "::";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 删除指定 cache 下所有分页缓存（匹配 "cacheName::page:*" 前缀）
     *
     * @param cacheName @Cacheable 注解中的 value 值
     */
    public void evictPageKeys(String cacheName) {
        String pattern = cacheName + SEPARATOR + "page:*";
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("批量删除分页缓存：cache={}, 共{}条", cacheName, keys.size());
            }
        } catch (Exception e) {
            log.warn("删除分页缓存失败：cache={}, pattern={}", cacheName, pattern, e);
        }
    }
}
