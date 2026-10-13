package com.smart.mapper;

import com.smart.entity.SeckillVoucher;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

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
     * 查出指定时间之后结束的秒杀券，供对账任务遍历。
     * <p>
     * 只取还在有效期内的（往前留一天余量，覆盖刚结束但可能还有在途请求的券），
     * 避免全表扫描，也避免给早已结束的券重建 Redis 库存 key。
     */
    @Select("select * from tb_seckill_voucher where end_time > #{since}")
    List<SeckillVoucher> listForReconcile(@Param("since") LocalDateTime since);

    /**
     * 删除秒杀券信息
     */
    @Delete("delete from tb_seckill_voucher where voucher_id = #{voucherId}")
    void deleteByVoucherId(Long voucherId);

}
