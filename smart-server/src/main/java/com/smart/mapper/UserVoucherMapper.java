package com.smart.mapper;

import com.smart.entity.UserVoucher;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户优惠券数据访问层
 */
@Mapper
public interface UserVoucherMapper {

    /**
     * 新增领取记录。
     * <p>
     * 不用 @AutoFill：这张表没有 create_user / update_user 列，时间直接由 SQL 写入。
     * 表上有 uk_user_voucher(user_id, voucher_id) 唯一索引，重复领取会抛 DuplicateKeyException。
     */
    @Insert("insert into tb_user_voucher (user_id, voucher_id, status, claim_time, create_time, update_time) " +
            "values (#{userId}, #{voucherId}, #{status}, #{claimTime}, now(), now())")
    void insert(UserVoucher userVoucher);

    /**
     * 查询某用户的全部券（含已使用，按领取时间倒序）—— 用于「我的优惠券」列表
     */
    @Select("select * from tb_user_voucher where user_id = #{userId} order by claim_time desc")
    List<UserVoucher> listByUserId(Long userId);

    /**
     * 查询某用户某张券的领取记录（下单用券时校验归属与状态）
     */
    @Select("select * from tb_user_voucher where user_id = #{userId} and voucher_id = #{voucherId}")
    UserVoucher getByUserAndVoucher(@Param("userId") Long userId, @Param("voucherId") Long voucherId);

    /**
     * 标记券为已使用（状态CAS）：只有「未使用(0)」才能改成「已使用(1)」，
     * 并发下同一张券被两个订单同时使用只会成功一次，另一个影响 0 行。
     *
     * @return 影响行数：1=本次用券成功；0=券已被别的订单用掉
     */
    @Update("update tb_user_voucher set status = #{used}, order_id = #{orderId}, " +
            "use_time = now(), update_time = now() " +
            "where user_id = #{userId} and voucher_id = #{voucherId} and status = #{unused}")
    int markUsed(@Param("userId") Long userId,
                 @Param("voucherId") Long voucherId,
                 @Param("orderId") Long orderId,
                 @Param("unused") Integer unused,
                 @Param("used") Integer used);

    /**
     * 统计某张券一共发出去多少份（领取记录条数）。
     * <p>
     * 秒杀券改造后库存存在 Redis，数据库的 tb_seckill_voucher.stock 变成"总发放额度"，
     * 于是"剩余可领量"要靠 {@code 额度 - 已发份数} 现算 —— 懒预热、对账任务、后台改库存都要用这个方法。
     * <p>
     * 依赖 db/33_user_voucher_index.sql 里新增的 idx_voucher(voucher_id)，否则是全表扫描。
     */
    @Select("select count(*) from tb_user_voucher where voucher_id = #{voucherId}")
    int countByVoucherId(@Param("voucherId") Long voucherId);

    /**
     * 查出某张券的所有领取人 id。
     * <p>
     * 对账用：修复 Redis 的 claim 集合 —— 如果某用户在数据库里有领取记录、
     * 但 Redis 集合里没有（比如回补时误删、或 Redis 被清空），把他补回去。
     * 这一步只增不减，不会破坏任何已有状态。
     */
    @Select("select user_id from tb_user_voucher where voucher_id = #{voucherId}")
    List<Long> listUserIdsByVoucherId(@Param("voucherId") Long voucherId);
}
