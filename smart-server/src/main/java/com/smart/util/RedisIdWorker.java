package com.smart.util;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 全局唯一ID生成器 —— 雪花算法（Snowflake）
 * <p>
 * 生成的ID是一个 64 位 Long 型整数，各段位含义如下（从左到右）：
 * <pre>
 *  0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 *  |                           时间戳                          | 数据中心 | 机器 | 序列号
 *  |                       41位（毫秒级）                      | 5位      | 5位   | 12位
 * </pre>
 * <ul>
 *   <li><b>符号位（1位）</b>：恒为 0，保证生成的 ID 为正数</li>
 *   <li><b>时间戳（41位）</b>：相对自定义起始时间（START_TIMESTAMP）的毫秒数，约可用 69 年</li>
 *   <li><b>数据中心ID（5位）</b>：最多支持 32 个数据中心</li>
 *   <li><b>机器ID（5位）</b>：每个数据中心最多支持 32 台机器</li>
 *   <li><b>序列号（12位）</b>：同一毫秒内的自增序列，每毫秒最多生成 4096 个 ID</li>
 * </ul>
 * <p>
 * 由于时间戳单调递增、机器ID各自独立、同毫秒内序列号自增，
 * 因此单机生成的 ID 全局唯一且趋势递增（随时间增大）。
 */
public class RedisIdWorker {

    // ==================== 雪花算法位分配常量 ====================

    /** 起始时间戳：2024-01-01 00:00:00（毫秒级），自定义业务纪元，用于缩短ID长度 */
    private static final long START_TIMESTAMP = 1704067200000L;

    /** 时间戳占用的位数：41位（约可用 2^41 毫秒 ≈ 69 年） */
    private static final long TIMESTAMP_BITS = 41L;

    /** 数据中心ID占用的位数：5位 */
    private static final long DATACENTER_BITS = 5L;

    /** 机器ID占用的位数：5位 */
    private static final long WORKER_BITS = 5L;

    /** 序列号占用的位数：12位 */
    private static final long SEQUENCE_BITS = 12L;

    // ==================== 位运算派生常量 ====================

    /** 数据中心ID最大值：31（5位全1） */
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_BITS);

    /** 机器ID最大值：31（5位全1） */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_BITS);

    /** 序列号最大值：4095（12位全1） */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /** 机器ID左移位数：12（序列号占 12 位，机器ID紧跟其后） */
    private static final long WORKER_SHIFT = SEQUENCE_BITS;

    /** 数据中心ID左移位数：17（序列号 + 机器ID共占 17 位） */
    private static final long DATACENTER_SHIFT = SEQUENCE_BITS + WORKER_BITS;

    /** 时间戳左移位数：22（序列号 + 机器ID + 数据中心ID共占 22 位） */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_BITS + DATACENTER_BITS;

    // ==================== 运行时状态 ====================

    /** 数据中心ID（部署时指定，同一数据中心内不同机器ID不能重复） */
    private final long datacenterId;

    /** 机器ID（部署时指定，同一数据中心内每台机器唯一） */
    private final long workerId;

    /** 上一次生成ID的时间戳（毫秒级），用于判断是否处于同一毫秒及时钟是否回拨 */
    private long lastTimestamp = -1L;

    /** 同一毫秒内的序列号，从 0 开始自增 */
    private long sequence = 0L;

    /** Redis 模板（预留：可用于序列号持久化、多实例协调等扩展场景，当前未使用） */
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 默认构造器：数据中心ID 与 机器ID 均取 0
     *
     * @param stringRedisTemplate Redis 操作模板
     */
    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this(stringRedisTemplate, 0L, 0L);
    }

    /**
     * 构造器：指定数据中心ID 与 机器ID
     *
     * @param stringRedisTemplate Redis 操作模板
     * @param datacenterId        数据中心ID（0 ~ 31）
     * @param workerId            机器ID（0 ~ 31）
     */
    public RedisIdWorker(StringRedisTemplate stringRedisTemplate, long datacenterId, long workerId) {
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId 不能大于 " + MAX_DATACENTER_ID + " 且不能小于 0");
        }
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("workerId 不能大于 " + MAX_WORKER_ID + " 且不能小于 0");
        }
        this.stringRedisTemplate = stringRedisTemplate;
        this.datacenterId = datacenterId;
        this.workerId = workerId;
    }

    /**
     * 生成全局唯一ID（雪花算法）
     * <p>
     * 生成逻辑：
     * <ol>
     *   <li>取当前系统时间戳（毫秒）</li>
     *   <li>若时间戳小于上次生成时间（时钟回拨），抛异常避免产生重复ID</li>
     *   <li>同一毫秒内：序列号 +1，若 12 位序列号用完则自旋等待进入下一毫秒；跨毫秒则序列号归 0</li>
     *   <li>将 时间戳 | 数据中心ID | 机器ID | 序列号 按位拼接返回</li>
     * </ol>
     *
     * @param keyPrefix 业务前缀（如 order、user），用于区分不同业务场景，当前保留用于日志识别与后续扩展
     * @return 全局唯一且趋势递增的 Long 型 ID
     */
    public synchronized Long nextId(String keyPrefix) {
        // 1. 获取当前系统时间戳（毫秒）
        long timestamp = System.currentTimeMillis();

        // 2. 时钟回拨校验：出现回拨说明系统时钟异常，为避免生成重复ID直接报错
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("系统时钟回拨，拒绝生成ID，回拨了 " + (lastTimestamp - timestamp) + " 毫秒");
        }

        // 3. 生成序列号
        if (lastTimestamp == timestamp) {
            // 3.1 同一毫秒内：序列号自增，并通过 & 运算使其在 0~4095 之间循环
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 3.2 序列号用尽（4095 + 1 = 4096，& 后归 0）：自旋等待到下一毫秒再生成
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 3.3 进入新的毫秒：序列号重新从 0 开始
            sequence = 0L;
        }

        // 4. 记录本次生成的时间戳，供下一次调用比对
        lastTimestamp = timestamp;

        // 5. 按位拼接生成最终ID：
        //    时间戳左移22位 | 数据中心ID左移17位 | 机器ID左移12位 | 序列号
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_SHIFT)
                | (workerId << WORKER_SHIFT)
                | sequence;
    }

    /**
     * 阻塞自旋等待，直到系统时间进入下一个毫秒
     *
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 新的（严格大于 lastTimestamp 的）时间戳
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
