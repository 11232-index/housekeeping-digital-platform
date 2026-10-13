package com.smart.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.smart.constant.MessageConstant;
import com.smart.context.BaseContext;
import com.smart.dto.VoucherDTO;
import com.smart.dto.VoucherPageQueryDTO;
import com.smart.entity.SeckillVoucher;
import com.smart.entity.UserVoucher;
import com.smart.entity.Voucher;
import com.smart.exception.VoucherBusinessException;
import com.smart.mapper.SeckillVoucherMapper;
import com.smart.mapper.UserMapper;
import com.smart.mapper.UserVoucherMapper;
import com.smart.mapper.VoucherMapper;
import com.smart.result.PageResult;
import com.smart.service.VoucherService;
import com.smart.util.RedisIdWorker;
import com.smart.vo.UserVoucherVO;
import com.smart.vo.VoucherVO;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 优惠券服务实现类
 * <p>
 * 领取记录有<b>两份</b>：
 * <ul>
 *   <li>Redis Set（key=claim:用户id，成员=优惠券id）—— 用来快速拦截重复领取；</li>
 *   <li>tb_user_voucher 表 —— 可靠来源，唯一索引兜底一人一单，
 *       同时是「我的优惠券」列表和下单抵扣的依据。</li>
 * </ul>
 * Redis 重启会丢记录，所以最终能不能领以数据库为准。
 */
@Slf4j
@Service
public class VoucherServiceImpl implements VoucherService {

    /** 用户领取记录 key 前缀（key=claim:用户id，Set 成员为已领取的优惠券id） */
    private static final String CLAIM_KEY_PREFIX = "claim:";

    /** 用户抢购分布式锁 key 前缀（Redisson RLock：lock:seckill:用户id，沿用原 key 便于排查） */
    private static final String LOCK_KEY_PREFIX = "lock:seckill:";

    /**
     * 秒杀库存 key 前缀（key=seckill:stock:优惠券id，值为字符串形式的剩余库存）。
     * <p>
     * 秒杀券的库存以这个 Redis key 为准：领券时由 seckill.lua 原子预扣，
     * 数据库 tb_seckill_voucher.stock 只记录初始库存、不再实时扣减。
     */
    private static final String SECKILL_STOCK_KEY = "seckill:stock:";

    // ==================== seckill.lua 的返回值约定（与 lua/seckill.lua 保持一致） ====================

    /** Lua 返回值：库存不足 */
    private static final long LUA_OK = 1L;

    /** Lua 返回值：库存不足 */
    private static final long LUA_STOCK_NOT_ENOUGH = 0L;

    /** Lua 返回值：该用户已领取过 */
    private static final long LUA_ALREADY_CLAIMED = -1L;

    /** Lua 返回值：库存未预热（Redis 里没有这张券的库存 key） */
    private static final long LUA_STOCK_NOT_PREHEATED = -2L;

    @Autowired
    private VoucherMapper voucherMapper;

    @Autowired
    private SeckillVoucherMapper seckillVoucherMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserVoucherMapper userVoucherMapper;

    /** 获取锁的最大等待时间（秒）：读配置 smart.lock.wait-time-sec（默认 5），拿不到锁时在此时间内阻塞等待（Redisson 发布订阅唤醒实现"锁重试等待"） */
    @Value("${smart.lock.wait-time-sec:5}")
    private long lockWaitTimeSec;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedisIdWorker redisIdWorker;

    /** Redisson 客户端：提供可重入分布式锁（RLock） */
    private final RedissonClient redissonClient;

    /** 秒杀预扣库存脚本（lua/seckill.lua）：一次原子完成「查库存 + 判一人一单 + 扣减 + 记领取人」 */
    private final RedisScript<Long> seckillScript;

    /**
     * 构造器：初始化 Redis 模板、全局ID生成器、Redisson 客户端与秒杀预扣脚本
     */
    public VoucherServiceImpl(StringRedisTemplate stringRedisTemplate,
                              RedissonClient redissonClient,
                              RedisScript<Long> seckillScript) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
        this.seckillScript = seckillScript;
        // 优惠券id使用全局ID生成器（RedisIdWorker）生成
        this.redisIdWorker = new RedisIdWorker(stringRedisTemplate);
    }

    // ==================== 员工端 CRUD ====================

    /**
     * 新增优惠券
     * 秒杀券（type=1）同步写入 tb_seckill_voucher（库存 + 有效时间）；普通券不涉及秒杀表
     */
    @Override
    @Transactional
    @CacheEvict(value = "voucher", allEntries = true)
    public void save(VoucherDTO dto) {
        Voucher voucher = new Voucher();
        BeanUtils.copyProperties(dto, voucher);
        // 优惠券id使用全局ID生成器（RedisIdWorker）生成
        voucher.setId(redisIdWorker.nextId("voucher"));
        // 原价非必填，为空时默认0（优惠按订单总金额 × 折扣率计算）
        if (voucher.getPayValue() == null) {
            voucher.setPayValue(0L);
        }
        voucherMapper.insert(voucher);
        log.info("新增优惠券，id={}", voucher.getId());
        // 秒杀券：同步写入秒杀表（库存 + 有效时间）
        if (Voucher.TYPE_SECKILL.equals(voucher.getType())) {
            // 校验生效时间必须早于失效时间
            if (dto.getBeginTime() != null && dto.getEndTime() != null && dto.getBeginTime().isAfter(dto.getEndTime())) {
                throw new VoucherBusinessException(MessageConstant.VOUCHER_TIME_INVALID);
            }
            SeckillVoucher seckillVoucher = SeckillVoucher.builder()
                    .voucherId(voucher.getId())
                    .stock(dto.getStock())
                    .beginTime(dto.getBeginTime())
                    .endTime(dto.getEndTime())
                    .build();
            seckillVoucherMapper.insert(seckillVoucher);
            // 预热库存到 Redis（key=seckill:stock:{id}）：秒杀请求会先在 Redis 里原子预扣。
            // 新建的券此时已发份数为 0，所以额度 = 剩余 = 表单值，直接写。
            stringRedisTemplate.opsForValue()
                    .set(SECKILL_STOCK_KEY + voucher.getId(), String.valueOf(dto.getStock()));
            log.info("新增秒杀券信息，voucherId={}", voucher.getId());
        }
    }

    /**
     * 分页查询优惠券（返回含秒杀信息的 VO）
     */
    @Override
    @Cacheable(value = "voucher", key = "'page:' + #dto.page + ':' + #dto.pageSize + ':' + #dto.title + ':' + #dto.type + ':' + #dto.status", sync = true)
    public PageResult pageQuery(VoucherPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Voucher> page = voucherMapper.pageQuery(dto);
        List<VoucherVO> voList = page.getResult().stream().map(this::toVoucherVO).collect(Collectors.toList());
        return new PageResult(page.getTotal(), voList);
    }

    /**
     * 根据id查询优惠券（含秒杀信息）
     */
    @Override
    @Cacheable(value = "voucher", key = "'one:' + #id", sync = true)
    public VoucherVO getById(Long id) {
        return toVoucherVO(voucherMapper.getById(id));
    }

    /**
     * 修改优惠券
     * 秒杀券同步更新/补充 tb_seckill_voucher；改为普通券时清理残留的秒杀信息
     */
    @Override
    @Transactional
    @CacheEvict(value = "voucher", allEntries = true)
    public void update(VoucherDTO dto) {
        Voucher voucher = new Voucher();
        BeanUtils.copyProperties(dto, voucher);
        voucherMapper.update(voucher);
        log.info("修改优惠券，id={}", voucher.getId());
        // 秒杀券：同步更新/补充秒杀表
        if (Voucher.TYPE_SECKILL.equals(voucher.getType())) {
            // 校验生效时间必须早于失效时间
            if (dto.getBeginTime() != null && dto.getEndTime() != null && dto.getBeginTime().isAfter(dto.getEndTime())) {
                throw new VoucherBusinessException(MessageConstant.VOUCHER_TIME_INVALID);
            }
            SeckillVoucher old = seckillVoucherMapper.getByVoucherId(voucher.getId());
            if (old == null) {
                SeckillVoucher seckillVoucher = SeckillVoucher.builder()
                        .voucherId(voucher.getId())
                        .stock(dto.getStock())
                        .beginTime(dto.getBeginTime())
                        .endTime(dto.getEndTime())
                        .build();
                seckillVoucherMapper.insert(seckillVoucher);
                // 由普通券改成秒杀券：把表单值当剩余量写进 Redis
                stringRedisTemplate.opsForValue()
                        .set(SECKILL_STOCK_KEY + voucher.getId(), String.valueOf(dto.getStock()));
                log.info("补充秒杀券信息，voucherId={}", voucher.getId());
            } else {
                SeckillVoucher seckillVoucher = new SeckillVoucher();
                seckillVoucher.setVoucherId(voucher.getId());
                // ⚠️ 语义换算：后台表单填的是「剩余可领数量」，而数据库的 stock 存的是「总发放额度」，
                //    所以落库前要换算：额度 = 剩余 + 已发份数。
                //    这个换算是幂等的 —— 不碰库存直接保存时 额度 = 剩余 + 已发 = 原额度，不会吃掉已发份额。
                int issued = userVoucherMapper.countByVoucherId(voucher.getId());
                int remaining = dto.getStock() == null ? 0 : dto.getStock();
                seckillVoucher.setStock(remaining + issued);
                seckillVoucher.setBeginTime(dto.getBeginTime());
                seckillVoucher.setEndTime(dto.getEndTime());
                seckillVoucher.setUpdateTime(LocalDateTime.now());
                seckillVoucherMapper.update(seckillVoucher);
                // Redis 里存的就是「剩余可领」，表单值可以直接写进去
                stringRedisTemplate.opsForValue()
                        .set(SECKILL_STOCK_KEY + voucher.getId(), String.valueOf(remaining));
                log.info("更新秒杀券信息，voucherId={}", voucher.getId());
            }
        } else {
            // 改为普通券：清理可能残留的秒杀信息
            seckillVoucherMapper.deleteByVoucherId(voucher.getId());
            // Redis 里的库存 key 也要删掉，否则会留下一个再也用不到的残留 key
            stringRedisTemplate.delete(SECKILL_STOCK_KEY + voucher.getId());
        }
    }

    /**
     * 上架/下架/过期
     */
    @Override
    @CacheEvict(value = "voucher", allEntries = true)
    public void startOrStop(Integer status, Long id) {
        Voucher voucher = Voucher.builder().status(status).id(id).build();
        voucherMapper.startOrStop(voucher);
        log.info("上架/下架/过期优惠券，id={}, status={}", id, status);
    }

    /**
     * 删除优惠券（同步删除秒杀信息）
     */
    @Override
    @Transactional
    @CacheEvict(value = "voucher", allEntries = true)
    public void delete(Long id) {
        voucherMapper.delete(id);
        seckillVoucherMapper.deleteByVoucherId(id);
        // 同步清掉 Redis 里的库存 key，避免残留
        stringRedisTemplate.delete(SECKILL_STOCK_KEY + id);
        log.info("删除优惠券，id={}", id);
    }

    // ==================== C端 ====================

    /**
     * 查询上架中的优惠券（含秒杀信息）
     */
    @Override
    @Cacheable(value = "voucher", key = "'on-sale'", sync = true)
    public List<VoucherVO> listOnSale() {
        // 过期秒杀券由 SQL 过滤（status=1 且 end_time > now），"过期"由截止时间自动判断，不再持久化
        List<Voucher> vouchers = voucherMapper.listOnSale();
        return vouchers.stream().map(this::toVoucherVO).collect(Collectors.toList());
    }

    /**
     * 领取/秒杀优惠券
     * <p>
     * 秒杀券流程：查询优惠券信息 -> 判断秒杀是否开始 -> 判断库存是否充足 -> 判断用户id是否存在
     * -> 判断是否在有效时间内（开始时间到结束时间）且上架(status=1) -> Redisson 分布式锁(RLock)锁用户id
     * -> 乐观锁扣减库存（防超卖）-> 记录领取 -> 返回
     * <p>
     * 普通券：直接领取，无库存/时间限制，仅判断用户存在、上架状态与一人一单。
     */
    @Override
    @Transactional
    // 精准失效：领取/秒杀只影响 “上架列表(on-sale)” 与该券详情(one:id) 两个缓存，
    // 不再用 @CacheEvict(allEntries=true) 每次全量清空 voucher 缓存，避免高并发领券时反复清空首页热缓存而自造缓存击穿/雪崩
    @Caching(evict = {
            @CacheEvict(value = "voucher", key = "'on-sale'"),
            @CacheEvict(value = "voucher", key = "'one:' + #voucherId")
    })
    public VoucherVO seckillVoucher(Long voucherId) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户领取/秒杀优惠券：userId={}, voucherId={}", userId, voucherId);

        // 1. 查询优惠券信息
        Voucher voucher = voucherMapper.getById(voucherId);
        if (voucher == null) {
            throw new VoucherBusinessException(MessageConstant.VOUCHER_NOT_FOUND);
        }

        // 普通优惠券：直接领取（无库存/时间限制）
        if (Voucher.TYPE_NORMAL.equals(voucher.getType())) {
            log.info("普通优惠券直接领取：voucherId={}, userId={}", voucherId, userId);
            return claimNormalVoucher(userId, voucherId);
        }

        // ==================== 秒杀券逻辑 ====================

        // 2. 判断秒杀是否开始
        SeckillVoucher seckillVoucher = seckillVoucherMapper.getByVoucherId(voucherId);
        LocalDateTime now = LocalDateTime.now();
        if (seckillVoucher == null || now.isBefore(seckillVoucher.getBeginTime())) {
            throw new VoucherBusinessException(MessageConstant.SECKILL_NOT_STARTED);
        }

        // 3. 库存判断不在这里做了 —— 改造后由下面的 Redis 预扣（seckill.lua）原子承担。
        //    注意：数据库的 stock 已变成「总发放额度」、不再随领取递减，
        //    再拿它判库存会得出错误结论 —— 真正的剩余量在 Redis 里。

        // 4. 判断用户id是否存在
        if (userMapper.getById(userId) == null) {
            throw new VoucherBusinessException(MessageConstant.USER_NOT_EXIST);
        }

        // 5. 判断是否在有效时间内（开始时间到结束时间）且优惠券上架（status=1）
        if (now.isBefore(seckillVoucher.getBeginTime())
                || now.isAfter(seckillVoucher.getEndTime())
                || !Voucher.STATUS_ON_SALE.equals(voucher.getStatus())) {
            throw new VoucherBusinessException(MessageConstant.VOUCHER_NOT_ON_SALE);
        }

        // 6. Redisson 可重入锁（RLock）给用户id加锁，解决一人一单
        //    - 可重入：内部以 hash 保存（field=线程标识，value=重入次数），同一线程可重复获取
        //    - 重试等待：tryLock(waitTime) 拿不到锁时阻塞等待最多 waitTime 秒（发布订阅唤醒，避免空转轮询）
        //    - 自动续期：不传 leaseTime 由 WatchDog 每 1/3 锁超时自动续期；线程结束才释放，宕机后也能按期回收
        RLock lock = redissonClient.getLock(LOCK_KEY_PREFIX + userId);
        boolean isLock;
        try {
            isLock = lock.tryLock(lockWaitTimeSec, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // 等待锁期间线程被中断：恢复中断标志后按抢锁失败处理
            Thread.currentThread().interrupt();
            throw new VoucherBusinessException(MessageConstant.VOUCHER_ALREADY_CLAIMED);
        }
        if (!isLock) {
            throw new VoucherBusinessException(MessageConstant.VOUCHER_ALREADY_CLAIMED);
        }
        try {
            // 7. 一人一单 + 库存预扣：Lua 脚本原子完成（脚本见 resources/lua/seckill.lua）
            //
            //    这一步替代了原来的 checkClaimed() + seckillVoucherMapper.deductStock()。
            //    原来的写法要 update 数据库那一行库存，所有并发请求抢同一行、被行锁串行化
            //    （实测 310 并发只换来约 2.4 倍吞吐）；搬到 Redis 后这个行锁竞争不复存在。
            //    checkClaimed 也一并省略：Lua 里的 sismember 已在同一次原子执行中做了同样的判断。
            long result = runSeckillScript(userId, voucherId, seckillVoucher);

            if (result == LUA_ALREADY_CLAIMED) {
                throw new VoucherBusinessException(MessageConstant.VOUCHER_ALREADY_CLAIMED);
            }
            if (result == LUA_STOCK_NOT_ENOUGH) {
                throw new VoucherBusinessException(MessageConstant.STOCK_NOT_ENOUGH);
            }
            if (result != LUA_OK) {
                // 懒预热重试后仍拿不到库存：说明连重建都建不出来（库里没有这张券的秒杀记录），属异常态
                throw new VoucherBusinessException(MessageConstant.SECKILL_STOCK_NOT_PREHEATED);
            }
            log.info("秒杀 Redis 预扣成功（库存已扣减、领取人已记录）：voucherId={}, userId={}", voucherId, userId);

            // 事务兜底：只要最终没提交，就把 Redis 这边的扣减还回去。
            // 不能只靠 catch 抓 insert 的异常 —— finally 里的 unlock()、落库后的查询、
            // 方法上的 @CacheEvict、乃至提交阶段本身抛异常，都不会走进那个 catch，
            // 而那些情况下 Redis 已经扣了、数据库却没记录，会永久少卖一张。
            AtomicBoolean claimedInDb = new AtomicBoolean(false);
            registerRollbackCompensation(userId, voucherId, claimedInDb);

            // 8. 落库并返回（Redis 那侧已完成，这里只把领取记录写进 tb_user_voucher）
            return doClaim(userId, voucherId, claimedInDb);
        } finally {
            // 仅当锁仍由当前线程持有时才释放，避免 WatchDog 到期或已被释放时误操作
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 普通券领取：判断用户存在 + 上架状态 + 一人一单（Redisson 分布式锁）
     */
    private VoucherVO claimNormalVoucher(Long userId, Long voucherId) {
        // 判断用户id是否存在
        if (userMapper.getById(userId) == null) {
            throw new VoucherBusinessException(MessageConstant.USER_NOT_EXIST);
        }
        // 判断优惠券是否上架
        Voucher voucher = voucherMapper.getById(voucherId);
        if (voucher == null || !Voucher.STATUS_ON_SALE.equals(voucher.getStatus())) {
            throw new VoucherBusinessException(MessageConstant.VOUCHER_NOT_ON_SALE);
        }
        // Redisson 可重入锁（RLock）给用户id加锁，解决一人一单（可重入/等待/续期说明同 seckillVoucher）
        RLock lock = redissonClient.getLock(LOCK_KEY_PREFIX + userId);
        boolean isLock;
        try {
            isLock = lock.tryLock(lockWaitTimeSec, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // 等待锁期间线程被中断：恢复中断标志后按抢锁失败处理
            Thread.currentThread().interrupt();
            throw new VoucherBusinessException(MessageConstant.VOUCHER_ALREADY_CLAIMED);
        }
        if (!isLock) {
            throw new VoucherBusinessException(MessageConstant.VOUCHER_ALREADY_CLAIMED);
        }
        try {
            // 一人一单：同一用户不能对同一优惠券重复领取
            checkClaimed(userId, voucherId);
            return doClaim(userId, voucherId);
        } finally {
            // 仅当锁仍由当前线程持有时才释放，避免 WatchDog 到期或已被释放时误操作
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 一人一单校验：该用户是否已领取过该优惠券
     */
    private void checkClaimed(Long userId, Long voucherId) {
        Boolean isMember = stringRedisTemplate.opsForSet()
                .isMember(CLAIM_KEY_PREFIX + userId, String.valueOf(voucherId));
        if (Boolean.TRUE.equals(isMember)) {
            throw new VoucherBusinessException(MessageConstant.VOUCHER_ALREADY_CLAIMED);
        }
    }

    /**
     * 记录领取关系并返回优惠券信息
     * 记录：key=claim:用户id，Set 成员=已领取的优惠券id
     */
    /**
     * 执行秒杀预扣脚本；遇到「库存未预热」时现场懒预热并重试一次。
     * <p>
     * 为什么需要懒预热：库存 key 只在 save()/update() 时写入，所以下面几种情况第一次请求必然拿不到它 ——
     * 改造前就存在的老券、直接 SQL 导入的数据、Redis 被清空过。
     * 这里按「总额度 - 已发份数」现场算出剩余量补写进去再重试一次，于是上线不需要任何数据预热脚本。
     * <p>
     * 用 setIfAbsent 而非 set：并发下只让第一个请求写入，不会覆盖掉别的请求刚做的那次 DECR。
     *
     * @return 1=抢到 0=库存不足 -1=已领取过 -2=重建后仍拿不到库存（异常态）
     */
    private long runSeckillScript(Long userId, Long voucherId, SeckillVoucher seckillVoucher) {
        long code = executeSeckillScript(userId, voucherId);
        if (code != LUA_STOCK_NOT_PREHEATED) {
            return code;
        }
        int quota = seckillVoucher.getStock() == null ? 0 : seckillVoucher.getStock();
        int issued = userVoucherMapper.countByVoucherId(voucherId);
        int remaining = Math.max(0, quota - issued);
        Boolean written = stringRedisTemplate.opsForValue()
                .setIfAbsent(SECKILL_STOCK_KEY + voucherId, String.valueOf(remaining));
        log.warn("秒杀库存未预热，已按「总额度 {} - 已发 {}」重建为 {}：voucherId={}, 写入结果={}",
                quota, issued, remaining, voucherId, written);
        return executeSeckillScript(userId, voucherId);
    }

    /**
     * 单纯执行一次秒杀预扣脚本，把 null 归一成「未预热」。
     * <p>
     * 返回值刻意用基本类型 long 再参与比较：脚本结果是从 Redis 回来的 Long 对象，
     * 若直接拿 Long 与常量做 ==，超出 -128~127 缓存范围时比的是引用而不是值。
     */
    private long executeSeckillScript(Long userId, Long voucherId) {
        Long raw = stringRedisTemplate.execute(
                seckillScript,
                Arrays.asList(SECKILL_STOCK_KEY + voucherId, CLAIM_KEY_PREFIX + userId),
                String.valueOf(voucherId));
        return raw == null ? LUA_STOCK_NOT_PREHEATED : raw;
    }

    /**
     * 注册「事务最终没提交就把 Redis 的扣减还回去」的兜底逻辑。
     * <p>
     * 为什么不放在 catch 里：catch 只覆盖"方法体抛异常"。下面这些情况都不会走进 catch，
     * 但事务一样会回滚、Redis 一样已经扣过：
     * <ul>
     *   <li>finally 里的 lock.unlock() 抛异常（Redis 抖动）</li>
     *   <li>落库之后的查询、转 VO 抛异常</li>
     *   <li>方法上的 @CacheEvict 抛异常（Redis 不可用时它就会抛）</li>
     *   <li>提交阶段本身失败（连接断开、刷盘报错等）</li>
     * </ul>
     * 用 afterCompletion 由事务的最终结果来驱动，一次覆盖全部路径。
     * <p>
     * 约束：afterCompletion 执行时事务已结束，但数据库连接仍绑定在 ThreadLocal 上，
     * 这里只能做纯 Redis 操作 —— 绝不能查库，否则会落到一个已完成的连接上。
     * <p>
     * 另外：回滚路径上"扣了又还"净变化为零，所以不需要额外去清缓存。
     */
    private void registerRollbackCompensation(Long userId, Long voucherId, AtomicBoolean claimedInDb) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            log.warn("事务同步未激活，秒杀回补兜底不生效：userId={}, voucherId={}", userId, voucherId);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_COMMITTED) {
                    return;
                }
                try {
                    stringRedisTemplate.opsForValue().increment(SECKILL_STOCK_KEY + voucherId);
                    // 数据库里本来就有领取记录（唯一键冲突）说明这个标记该在，不能撤
                    if (!claimedInDb.get()) {
                        stringRedisTemplate.opsForSet()
                                .remove(CLAIM_KEY_PREFIX + userId, String.valueOf(voucherId));
                    }
                    log.warn("秒杀事务未提交，已回补 Redis（库存已归还）：userId={}, voucherId={}", userId, voucherId);
                } catch (Exception e) {
                    // 回补失败只能记日志，交给对账任务兜底。
                    // 这里绝不能再往外抛异常 —— 那会掩盖导致事务回滚的原始业务异常。
                    log.error("秒杀 Redis 回补失败，等待对账任务修复：userId={}, voucherId={}, {}",
                            userId, voucherId, e.getMessage(), e);
                }
            }
        });
    }

    private VoucherVO doClaim(Long userId, Long voucherId) {
        return doClaim(userId, voucherId, new AtomicBoolean(false));
    }

    private VoucherVO doClaim(Long userId, Long voucherId, AtomicBoolean claimedInDb) {
        // Redis 只做快速拦截；真正的领取记录落在 tb_user_voucher
        // 这里保留 SADD：普通券路径（claimNormalVoucher）没有走 Lua，它的领取标记必须由这里写。
        // 秒杀路径下 seckill.lua 里已经加过一次，SADD 幂等，是 no-op，代价可忽略。
        stringRedisTemplate.opsForSet().add(CLAIM_KEY_PREFIX + userId, String.valueOf(voucherId));
        UserVoucher record = UserVoucher.builder()
                .userId(userId)
                .voucherId(voucherId)
                .status(UserVoucher.STATUS_UNUSED)
                .claimTime(LocalDateTime.now())
                .build();
        try {
            userVoucherMapper.insert(record);
        } catch (DuplicateKeyException e) {
            // 数据库里本来就有这条领取记录 —— 标记一下，让事务回补时只还库存、不撤这个标记
            claimedInDb.set(true);
            // Redis 记录丢了（比如重启），但数据库唯一索引把重复领取拦住了
            throw new VoucherBusinessException(MessageConstant.VOUCHER_ALREADY_CLAIMED);
        }
        log.info("优惠券领取成功：userId={}, voucherId={}", userId, voucherId);
        return toVoucherVO(voucherMapper.getById(voucherId));
    }

    /**
     * 查询某用户的全部优惠券（含已使用，按领取时间倒序）—— C端「我的优惠券」。
     * 已使用的也返回，由前端置灰展示；下单选券时前端只挑未使用的。
     */
    @Override
    public List<UserVoucherVO> listMyVouchers(Long userId) {
        List<UserVoucher> records = userVoucherMapper.listByUserId(userId);
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }
        List<UserVoucherVO> list = new ArrayList<>(records.size());
        for (UserVoucher record : records) {
            Voucher voucher = voucherMapper.getById(record.getVoucherId());
            if (voucher == null) {
                // 券模板已被删掉，这条领取记录没有展示意义，跳过
                continue;
            }
            list.add(UserVoucherVO.builder()
                    .id(record.getId())
                    .voucherId(voucher.getId())
                    .title(voucher.getTitle())
                    .subTitle(voucher.getSubTitle())
                    .rules(voucher.getRules())
                    .actualValue(voucher.getActualValue())
                    .type(voucher.getType())
                    .status(record.getStatus())
                    .claimTime(record.getClaimTime())
                    .useTime(record.getUseTime())
                    .build());
        }
        log.info("查询我的优惠券：userId={}, 共{}条", userId, list.size());
        return list;
    }

    /**
     * 优惠券转VO（秒杀券补充库存和有效时间）
     */
    /**
     * 读取某张秒杀券的「剩余可领数量」——以 Redis 为准。
     * <p>
     * 注意**不能**在读不到 Redis 时直接返回数据库的 stock：那个值现在是「总发放额度」，
     * 一旦 Redis 丢了 key，详情页会显示"库存回到满额"，与事实相反。
     * 正确做法是按「总额度 - 已发份数」现算，并用 setIfAbsent 顺手写回 Redis（懒预热），
     * 让这条兜底路径本身是自愈的。
     */
    private Integer readRemainingStock(Long voucherId, Integer quota) {
        int quotaValue = quota == null ? 0 : quota;
        try {
            String value = stringRedisTemplate.opsForValue().get(SECKILL_STOCK_KEY + voucherId);
            if (value != null) {
                return Integer.valueOf(value);
            }
            int issued = userVoucherMapper.countByVoucherId(voucherId);
            int remaining = Math.max(0, quotaValue - issued);
            stringRedisTemplate.opsForValue()
                    .setIfAbsent(SECKILL_STOCK_KEY + voucherId, String.valueOf(remaining));
            log.warn("读取库存时 Redis 无 key，已按「额度 {} - 已发 {}」懒预热为 {}：voucherId={}",
                    quotaValue, issued, remaining, voucherId);
            return remaining;
        } catch (Exception e) {
            // Redis 或数据库查询出问题都不能让详情页整个报错，退回"额度 - 已发"的现算值
            log.error("读取秒杀库存失败，退回现算值：voucherId={}, {}", voucherId, e.getMessage());
            try {
                return Math.max(0, quotaValue - userVoucherMapper.countByVoucherId(voucherId));
            } catch (Exception ignored) {
                return quotaValue;
            }
        }
    }

    private VoucherVO toVoucherVO(Voucher voucher) {
        if (voucher == null) {
            return null;
        }
        VoucherVO vo = new VoucherVO();
        BeanUtils.copyProperties(voucher, vo);
        if (Voucher.TYPE_SECKILL.equals(voucher.getType())) {
            SeckillVoucher sv = seckillVoucherMapper.getByVoucherId(voucher.getId());
            if (sv != null) {
                // 库存以 Redis 为准：数据库的 stock 是「总发放额度」，不是剩余量
                vo.setStock(readRemainingStock(voucher.getId(), sv.getStock()));
                vo.setBeginTime(sv.getBeginTime());
                vo.setEndTime(sv.getEndTime());
            }
        }
        return vo;
    }
}
