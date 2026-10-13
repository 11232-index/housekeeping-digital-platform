-- 秒杀优惠券：Redis 预扣库存脚本
--
-- 【为什么必须用 Lua】
-- 原实现靠数据库的行锁保证不超卖：
--     update tb_seckill_voucher set stock = stock - 1 where voucher_id = ? and stock > 0
-- 但那样会让所有并发请求抢「同一行」，InnoDB 只能一次放一个事务进去，
-- 写入路径被彻底串行化（实测 310 并发只换来约 2.4 倍吞吐）。
--
-- 把库存判断搬到 Redis 后，「读库存 → 判断 → 扣减 → 记领取人」是四步操作。
-- Redis 虽然是单线程的（单条命令天然不会并发），但这四步之间仍会被别人的命令插队，
-- 所以要把它们打包成一段脚本 —— Redis 会把整段脚本当成「一条命令」一口气执行完，
-- 中间不允许任何请求插进来。这样就不需要加锁，一次网络往返搞定。
--
-- 【KEYS】
--   KEYS[1] = seckill:stock:{voucherId}   该券的剩余库存（字符串形式的整数）
--   KEYS[2] = claim:{userId}              该用户已领取的 voucherId 集合（复用现有结构）
-- 【ARGV】
--   ARGV[1] = voucherId                   要领取的券 id
--
-- 【返回值】1 = 抢到   0 = 库存不足   -1 = 该用户已领取过   -2 = 库存未预热
--
-- 【前提】本脚本用到的两个 key 分属不同实体（一个按券、一个按用户）。
-- 单机 Redis 下没有问题；若将来部署 Redis Cluster，两个 key 可能落在不同 slot 而报错
-- （Redis Cluster 要求一次脚本里的所有 key 在同一 slot），届时需改用 hash tag 或拆分逻辑。

local stockStr = redis.call('get', KEYS[1])

-- 库存 key 不存在：说明这张券还没预热进 Redis，直接拒绝，绝不能当成「有库存」放行
if not stockStr then
    return -2
end

local stock = tonumber(stockStr)
-- 值不是整数（脏数据）—— 按「未预热」处理，让调用方能走懒预热重建，而不是当成卖光了
if not stock then
    return -2
end
if stock <= 0 then
    return 0
end

-- 一人一单：该用户的领取集合里已经有这张券了
if redis.call('sismember', KEYS[2], ARGV[1]) == 1 then
    return -1
end

-- 扣库存 + 记录领取人（与上面的判断同处一段脚本，不可能被插队，所以不会超卖）
redis.call('decr', KEYS[1])
redis.call('sadd', KEYS[2], ARGV[1])
return 1
