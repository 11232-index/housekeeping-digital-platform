package com.smart.mapper;

import com.smart.entity.SeckillVoucher;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SeckillVoucherMapper {

    /**
     * 新增秒杀券信息（库存 + 有效时间，时间字段由数据库默认值填充）
     */
    @Insert("insert into tb_seckill_voucher (voucher_id, stock, begin_time, end_time) " +
            "values (#{voucherId}, #{stock}, #{beginTime}, #{endTime})")
    void insert(SeckillVoucher seckillVoucher);

    /**
     * 根据优惠券id查询秒杀信息
     */
    @Select("select * from tb_seckill_voucher where voucher_id = #{voucherId}")
    SeckillVoucher getByVoucherId(Long voucherId);

    /**
     * 修改秒杀券信息（库存 + 有效时间）
     */
    void update(SeckillVoucher seckillVoucher);

    /**
     * 乐观锁扣减库存：仅当库存大于0时扣减，防止超卖
     * @return 受影响行数，0表示扣减失败（库存不足）
     */
    @Update("update tb_seckill_voucher set stock = stock - 1 where voucher_id = #{voucherId} and stock > 0")
    int deductStock(Long voucherId);

    /**
     * 删除秒杀券信息
     */
    @Delete("delete from tb_seckill_voucher where voucher_id = #{voucherId}")
    void deleteByVoucherId(Long voucherId);

}
