USE `smart_housekeeping_system`;

-- ============================================================
-- ⚠️⚠️  本文件只能执行一次！重复执行会把总额度算翻倍  ⚠️⚠️
--
-- 作用：把存量秒杀券的 stock 从"剩余库存"换算成"总发放额度"
--
-- 为什么必须换算：
--   改造前 stock 被历次领取递减过，所以老数据里它 = 剩余。
--   改造后代码把它当"总额度"用，按 `额度 - 已发份数` 去重建 Redis 库存。
--   如果不换算，就会把已经发出去的那部分再扣一遍 ——
--   例如：老券 stock=70、实际已发 30（原始额度 100），
--         不换算则算法认为剩余 = 70 - 30 = 40，凭空少发 30 张。
--
-- 执行顺序：先跑第 1 步核对，确认数字对得上，再手动跑第 2 步。
-- ============================================================


-- ---------- 第 1 步：先核对（只读，随便跑） ----------
-- 看「旧值_剩余 + 已发 = 新值_额度」是否等于你印象中的原始额度。
-- 只看最近一天内结束的券之后的（更早的券已经过期，对不上也不影响业务）。
SELECT sv.voucher_id,
       sv.stock                    AS 旧值_剩余,
       COUNT(uv.id)                AS 已发份数,
       sv.stock + COUNT(uv.id)     AS 新值_额度
FROM tb_seckill_voucher sv
         LEFT JOIN tb_user_voucher uv ON uv.voucher_id = sv.voucher_id
WHERE sv.end_time > NOW() - INTERVAL 1 DAY
GROUP BY sv.voucher_id, sv.stock;


-- ---------- 第 2 步：换算（⚠️ 确认过第 1 步结果后再执行，且只执行一次） ----------
-- 默认注释掉，需要时手动放开执行。
--
-- UPDATE tb_seckill_voucher sv
-- SET sv.stock = sv.stock + (
--     SELECT COUNT(*) FROM tb_user_voucher uv WHERE uv.voucher_id = sv.voucher_id)
-- WHERE sv.end_time > NOW() - INTERVAL 1 DAY;


-- ---------- 第 3 步：执行完之后，Redis 里的库存由代码里的懒预热/对账任务自动建立 ----------
-- 不需要手动往 Redis 写数据。
