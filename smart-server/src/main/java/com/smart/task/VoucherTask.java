package com.smart.task;

import com.smart.entity.SeckillVoucher;
import com.smart.mapper.SeckillVoucherMapper;
import com.smart.mapper.UserVoucherMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 优惠券相关的定时任务。
 * <p>
 * 目前只有一个：<b>秒杀库存对账</b>。
 * <p>
 * 为什么需要它：秒杀券改造后库存存在 Redis（见 lua/seckill.lua），而"谁领到了"存在 MySQL。
 * 两者之间靠"回补"维持一致，但回补本身也可能失败（Redis 抖动、进程被杀）。
 * 对账就是最后一道兜底 —— 定期比对两边的数字，把跑偏的 Redis 值拉回来。
 * <p>
 * ⚠️ 依赖：数据库的 tb_seckill_voucher.stock 必须是「总发放额度」。
 * 如果存量老券还没执行 db/34_migrate_seckill_stock_to_quota.sql 换算，
 * 本任务会误判（把老券的"剩余"当"额度"）并打出"已超卖"的假告警。
 */
@Component
@Slf4j
public class VoucherTask {

    /** 秒杀库存 key 前缀（与 VoucherServiceImpl.SECKILL_STOCK_KEY 保持一致） */
    private static final String SECKILL_STOCK_KEY = "seckill:stock:";

    /** 一人一单记录 key 前缀（与 VoucherServiceImpl.CLAIM_KEY_PREFIX 保持一致） */
    private static final String CLAIM_KEY_PREFIX = "claim:";

    /** 对账时间窗（小时）：只处理"结束时间晚于 now - 该值"的券，避免全表扫描、也不给早已结束的券重建 key */
    private static final int RECONCILE_WINDOW_HOURS = 24;

    /**
     * 上一轮算出的偏差，用于"抖动过滤"。
     * <p>
     * 为什么需要它：当某个请求"Lua 已扣减、事务还没提交"时，本轮统计到的已发份数 C 还没算上这一份，
     * 于是偏差 D 会短暂变成 +1。如果立刻修正，就会把在途的那一份还回去；等它提交后 D 又变成 -1，
     * 下一轮再扣回来 —— 在两值之间来回抖。
     * 要求"同一张券的偏差连续两轮一致"才动手，就能避开在途请求的干扰。
     * <p>
     * 用普通 HashMap 即可：@Scheduled 默认是单线程调度，三个任务共用一个线程，不存在并发访问。
     */
    private final Map<Long, Long> lastDeviation = new HashMap<>();

    @Autowired
    private SeckillVoucherMapper seckillVoucherMapper;

    @Autowired
    private UserVoucherMapper userVoucherMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CacheManager cacheManager;

    /**
     * 秒杀库存对账：每 5 分钟一次
     * <p>
     * 维护的不变量：<b>Redis 剩余库存 R == 数据库总额度 S - 数据库已发份数 C</b>
     * <p>
     * 偏差 <code>D = (S - C) - R</code> 的含义与处理：
     * <ul>
     *   <li><code>D == 0</code> → 一致，跳过</li>
     *   <li><code>D &gt; 0</code> → Redis 少扣了（回补失败、或 Redis 被清空后按额度重建过）→ 把 R 下调 D</li>
     *   <li><code>D &lt; 0</code> → Redis 多扣了 → 把 R 上调 |D|</li>
     * </ul>
     * 修正<b>必须</b>用 <code>INCRBY(D)</code> 这种"位移"方式，
     * <b>绝对不能用 SET 直接赋值</b> —— SET 会覆盖掉"统计 C 之后、SET 之前"发生的并发 DECR，
     * 那些已经发出去的券等于白送一份，也就是超卖。这正是本次改造最不能出现的问题。
     */
    @Scheduled(cron = "0 */5 * * * ?") //每 5 分钟对账一次
    public void reconcileSeckillStock() {
        LocalDateTime since = LocalDateTime.now().minusHours(RECONCILE_WINDOW_HOURS);
        List<SeckillVoucher> vouchers = seckillVoucherMapper.listForReconcile(since);
        if (vouchers == null || vouchers.isEmpty()) {
            return;
        }

        int fixed = 0;
        int alerted = 0;
        for (SeckillVoucher sv : vouchers) {
            try {
                Long voucherId = sv.getVoucherId();
                String key = SECKILL_STOCK_KEY + voucherId;
                int quota = sv.getStock() == null ? 0 : sv.getStock();
                int issued = userVoucherMapper.countByVoucherId(voucherId);

                // 发出去的比总额度还多 —— 已经超卖了，必须告警而不是静默"修好"
                if (issued > quota) {
                    log.error("【对账告警】秒杀券疑似超卖：voucherId={}, 总额度={}, 已发={}，建议立即下架该券",
                            voucherId, quota, issued);
                    alerted++;
                    lastDeviation.remove(voucherId);
                    continue;
                }

                String current = stringRedisTemplate.opsForValue().get(key);
                if (current == null) {
                    // 整个 key 不见了（Redis 被清空 / 重启丢数据）：按「额度 - 已发」重建
                    // 用 setIfAbsent 而不是 set，避免覆盖并发请求刚做的 DECR
                    int remaining = Math.max(0, quota - issued);
                    Boolean written = stringRedisTemplate.opsForValue()
                            .setIfAbsent(key, String.valueOf(remaining));
                    log.warn("对账发现 Redis 无库存 key，已重建为 {}：voucherId={}, 写入结果={}",
                            remaining, voucherId, written);
                    lastDeviation.remove(voucherId);
                    evictVoucherCache(voucherId);
                    fixed++;
                    continue;
                }

                long deviation = (long) (quota - issued) - Long.parseLong(current);
                Long previous = lastDeviation.put(voucherId, deviation);
                if (deviation == 0) {
                    continue;
                }
                // 抖动过滤：连续两轮偏差相同才修（排除在途请求造成的瞬时偏差）
                if (previous == null || previous != deviation) {
                    log.info("对账发现库存偏差，先记下、等下一轮确认：voucherId={}, 偏差={}", voucherId, deviation);
                    continue;
                }

                stringRedisTemplate.opsForValue().increment(key, deviation);
                log.warn("对账已修正 Redis 库存：voucherId={}, 偏差={}, 修正后剩余应为 {}",
                        voucherId, deviation, quota - issued);
                fixClaimSet(voucherId);
                evictVoucherCache(voucherId);
                fixed++;
            } catch (Exception e) {
                // 单张券出问题不影响其余券
                log.error("对账单张券失败：voucherId={}, {}", sv.getVoucherId(), e.getMessage());
            }
        }

        if (fixed > 0 || alerted > 0) {
            log.info("秒杀库存对账结束：扫描 {} 张券，修正 {} 张，告警 {} 张", vouchers.size(), fixed, alerted);
        }
    }

    /**
     * 顺手修复"一人一单"标记：把数据库里有领取记录、Redis 集合里却没有的用户补回去。
     * <p>
     * 这种不一致来自"回补时误删了标记"或"Redis 被清空"。虽然数据库唯一索引仍能拦住重复领取，
     * 但每次都要走一遍"插入失败 + 抛异常"的重路径，补回去更干净。
     * 本方法只增不减 —— 不会把任何该有的标记弄丢，所以是安全的。
     */
    private void fixClaimSet(Long voucherId) {
        List<Long> userIds = userVoucherMapper.listUserIdsByVoucherId(voucherId);
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds) {
            try {
                stringRedisTemplate.opsForSet()
                        .add(CLAIM_KEY_PREFIX + userId, String.valueOf(voucherId));
            } catch (Exception e) {
                log.error("修复领取标记失败：voucherId={}, userId={}, {}", voucherId, userId, e.getMessage());
            }
        }
    }

    /** 库存被修正后清掉相关缓存，避免详情页/首页继续显示旧库存 */
    private void evictVoucherCache(Long voucherId) {
        try {
            Cache cache = cacheManager.getCache("voucher");
            if (cache != null) {
                cache.evict("one:" + voucherId);
                cache.evict("on-sale");
            }
        } catch (Exception e) {
            log.error("清除优惠券缓存失败：voucherId={}, {}", voucherId, e.getMessage());
        }
    }
}
