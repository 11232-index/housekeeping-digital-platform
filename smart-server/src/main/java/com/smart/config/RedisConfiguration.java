package com.smart.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 配置类
 * 1. RedisTemplate —— 手动读写 Redis（如店铺营业状态 SHOP_STATUS）
 * 2. RedisCacheManager —— 让 @Cacheable / @CacheEvict 使用 Redis 作为缓存后端
 */
@Configuration
@Slf4j
public class RedisConfiguration {

    /**
     * 配置 RedisTemplate Bean
     * 用于手动操作 Redis，如 shop 模块的营业状态读写
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("=== RedisTemplate 初始化 ===");
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // Key 使用 String 序列化，方便在 Redis 中查看
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // Value 保持默认的 JDK 序列化，兼容已存在的 SHOP_STATUS 等数据
        return redisTemplate;
    }

    /**
     * 配置 RedisCacheManager Bean
     * 设为 @Primary 覆盖默认的内存 ConcurrentMapCacheManager
     *
     * 加上此 Bean 后，项目中所有 @Cacheable、@CacheEvict 注解
     * 以及手动注入的 CacheManager 都会自动使用 Redis 作为缓存后端。
     *
     * 缓存特性：
     * - 默认 TTL 30 分钟，超时自动淘汰
     * - Key 支持 String 序列化
     * - Value 使用 JDK 序列化（带类型信息，保证反序列化正确）
     * - 允许缓存 null 值（防缓存穿透）
     * - 写入时 TTL 随机抖动 +0~20%（防缓存雪崩）
     * - 应用重启后缓存不丢失（Redis 持久化到磁盘的数据不受影响）
     * - 注意：Redis 不可用时，@Cacheable 会直接抛出缓存异常、该接口报错，本工程暂未实现“自动降级回源 MySQL”的兜底
     */
    @Bean
    @Primary
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        log.info("=== RedisCacheManager 初始化（Spring Cache 后端切换为 Redis，TTL 随机抖动防雪崩）===");

        // 默认配置：30 分钟 TTL，允许缓存 null（防穿透）
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new JdkSerializationRedisSerializer())
                );
        // 注意：不调用 disableCachingNullValues()，Spring Data Redis 用 NullValue 哨兵代替 null

        // 短 TTL（5 分钟）：高频查询，穿透风险高
        RedisCacheConfiguration shortTtl = defaultConfig.entryTtl(Duration.ofMinutes(5));
        // 长 TTL（60 分钟）：几乎不变的数据
        RedisCacheConfiguration longTtl = defaultConfig.entryTtl(Duration.ofMinutes(60));
        Map<String, RedisCacheConfiguration> ttlMap = new HashMap<>();
        ttlMap.put("order", shortTtl);
        ttlMap.put("housekeeperSchedule", shortTtl);
        ttlMap.put("about", longTtl);
        ttlMap.put("serviceIntro", longTtl);
        ttlMap.put("updates", longTtl);
        ttlMap.put("housekeeperEmployee", longTtl);

        // 使用自定义写入器：写入时给 TTL 增加随机抖动，避免同批 key 同时过期（防雪崩）
        RedisCacheWriter cacheWriter = new RandomTtlRedisCacheWriter(redisConnectionFactory);

        return RedisCacheManager.builder(cacheWriter)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(ttlMap)
                .build();
    }

    /**
     * 配置 RedissonClient Bean —— 用于可重入的 Redis 分布式锁（RLock）
     * <p>
     * 提供如下能力（由 Redisson 内部实现，外部只管调用 API）：
     * - 可重入：锁以 hash 结构存储（field=线程标识，value=重入次数），同一线程可重复获取锁
     * - 重试等待：{@code tryLock(waitTime)} 拿不到锁时会阻塞等待，内部由"发布订阅"唤醒等待线程
     * - WatchDog 自动续期：tryLock 不传 leaseTime 时，锁默认 {@code smart.lock.watchdog-timeout-ms}（默认 30s）超时，
     *   并由 WatchDog 每 1/3 周期自动续期，线程正常结束才释放，进程宕机后锁也能在超时内自动回收，避免长占锁
     * <p>
     * 连接参数沿用 {@code smart.redis.*}（与缓存同库），保证同一 Redis 实例；
     * WatchDog 周期读取 {@code smart.lock.watchdog-timeout-ms}
     */
    @Bean
    public RedissonClient redissonClient(@Value("${smart.redis.host}") String host,
                                         @Value("${smart.redis.port}") int port,
                                         @Value("${smart.redis.database}") int database,
                                         @Value("${smart.lock.watchdog-timeout-ms:30000}") long watchdogTimeoutMs) {
        log.info("=== RedissonClient 初始化（host={}, port={}, database={}, lockWatchdogTimeout={}ms）===",
                host, port, database, watchdogTimeoutMs);
        Config config = new Config();
        // 设置 WatchDog 续期周期（毫秒），即锁的默认超时时间
        config.setLockWatchdogTimeout(watchdogTimeoutMs);
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setDatabase(database);
        return Redisson.create(config);
    }
}
