package com.smart.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.smart.constant.MessageConstant;
import com.smart.context.BaseContext;
import com.smart.dto.VoucherDTO;
import com.smart.dto.VoucherPageQueryDTO;
import com.smart.entity.SeckillVoucher;
import com.smart.entity.Voucher;
import com.smart.exception.VoucherBusinessException;
import com.smart.mapper.SeckillVoucherMapper;
import com.smart.mapper.UserMapper;
import com.smart.mapper.VoucherMapper;
import com.smart.result.PageResult;
import com.smart.service.VoucherService;
import com.smart.util.RedisIdWorker;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 优惠券服务实现类
 * <p>
 * 领取记录：使用 Redis Set 记录"哪个用户领取了哪张券"（key=claim:用户id，成员=优惠券id），
 * 用于一人一单校验，也是后续"服务/服务套餐支付时使用优惠券抵扣"的判断依据。
 */
@Slf4j
@Service
public class VoucherServiceImpl implements VoucherService {

    /** 用户领取记录 key 前缀（key=claim:用户id，Set 成员为已领取的优惠券id） */
    private static final String CLAIM_KEY_PREFIX = "claim:";

    /** 用户抢购分布式锁 key 前缀（Redisson RLock：lock:seckill:用户id，沿用原 key 便于排查） */
    private static final String LOCK_KEY_PREFIX = "lock:seckill:";

    @Autowired
    private VoucherMapper voucherMapper;

    @Autowired
    private SeckillVoucherMapper seckillVoucherMapper;

    @Autowired
    private UserMapper userMapper;

    /** 获取锁的最大等待时间（秒）：读配置 smart.lock.wait-time-sec（默认 5），拿不到锁时在此时间内阻塞等待（Redisson 发布订阅唤醒实现"锁重试等待"） */
    @Value("${smart.lock.wait-time-sec:5}")
    private long lockWaitTimeSec;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedisIdWorker redisIdWorker;

    /** Redisson 客户端：提供可重入分布式锁（RLock） */
    private final RedissonClient redissonClient;

    /**
     * 构造器：初始化 Redis 模板、全局ID生成器与 Redisson 客户端
     */
    public VoucherServiceImpl(StringRedisTemplate stringRedisTemplate, RedissonClient redissonClient) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
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
                log.info("补充秒杀券信息，voucherId={}", voucher.getId());
            } else {
                SeckillVoucher seckillVoucher = new SeckillVoucher();
                seckillVoucher.setVoucherId(voucher.getId());
                seckillVoucher.setStock(dto.getStock());
                seckillVoucher.setBeginTime(dto.getBeginTime());
                seckillVoucher.setEndTime(dto.getEndTime());
                seckillVoucher.setUpdateTime(LocalDateTime.now());
                seckillVoucherMapper.update(seckillVoucher);
                log.info("更新秒杀券信息，voucherId={}", voucher.getId());
            }
        } else {
            // 改为普通券：清理可能残留的秒杀信息
            seckillVoucherMapper.deleteByVoucherId(voucher.getId());
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

        // 3. 判断库存是否充足
        if (seckillVoucher.getStock() < 1) {
            throw new VoucherBusinessException(MessageConstant.STOCK_NOT_ENOUGH);
        }

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
            // 一人一单：同一用户不能对同一优惠券重复领取
            checkClaimed(userId, voucherId);
            // 7. 乐观锁扣减库存（仅当库存>0时扣减，防止超卖）
            int rows = seckillVoucherMapper.deductStock(voucherId);
            if (rows == 0) {
                throw new VoucherBusinessException(MessageConstant.STOCK_NOT_ENOUGH);
            }
            log.info("秒杀库存扣减成功：voucherId={}, 影响行数={}", voucherId, rows);
            // 8. 记录领取并返回
            return doClaim(userId, voucherId);
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
    private VoucherVO doClaim(Long userId, Long voucherId) {
        stringRedisTemplate.opsForSet().add(CLAIM_KEY_PREFIX + userId, String.valueOf(voucherId));
        log.info("优惠券领取成功：userId={}, voucherId={}", userId, voucherId);
        return toVoucherVO(voucherMapper.getById(voucherId));
    }

    /**
     * 优惠券转VO（秒杀券补充库存和有效时间）
     */
    private VoucherVO toVoucherVO(Voucher voucher) {
        if (voucher == null) {
            return null;
        }
        VoucherVO vo = new VoucherVO();
        BeanUtils.copyProperties(voucher, vo);
        if (Voucher.TYPE_SECKILL.equals(voucher.getType())) {
            SeckillVoucher sv = seckillVoucherMapper.getByVoucherId(voucher.getId());
            if (sv != null) {
                vo.setStock(sv.getStock());
                vo.setBeginTime(sv.getBeginTime());
                vo.setEndTime(sv.getEndTime());
            }
        }
        return vo;
    }
}
