package com.smart.mapper;

import com.github.pagehelper.Page;
import com.smart.annotation.AutoFill;
import com.smart.dto.VoucherPageQueryDTO;
import com.smart.entity.Voucher;
import com.smart.enumeration.OperationType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface VoucherMapper {

    /**
     * 新增优惠券（id 由 RedisIdWorker 全局ID生成器生成）
     */
    @AutoFill(OperationType.INSERT)
    @Insert("insert into tb_voucher (id, title, sub_title, rules, pay_value, actual_value, type, status, create_time, update_time) " +
            "values (#{id}, #{title}, #{subTitle}, #{rules}, #{payValue}, #{actualValue}, #{type}, #{status}, #{createTime}, #{updateTime})")
    void insert(Voucher voucher);

    /**
     * 分页查询优惠券
     */
    Page<Voucher> pageQuery(VoucherPageQueryDTO dto);

    /**
     * 根据id查询
     */
    @Select("select * from tb_voucher where id = #{id}")
    Voucher getById(Long id);

    /**
     * 修改优惠券
     */
    @AutoFill(OperationType.UPDATE)
    void update(Voucher voucher);

    /**
     * 上架/下架/过期（status：1上架；2下架；3过期）
     */
    @AutoFill(OperationType.UPDATE)
    @Update("update tb_voucher set status = #{status}, update_time = #{updateTime} where id = #{id}")
    void startOrStop(Voucher voucher);

    /**
     * 删除
     */
    @Delete("delete from tb_voucher where id = #{id}")
    void delete(Long id);

    /**
     * 查询上架中的优惠券（秒杀券还需未过失效时间，过期的不展示）
     */
    @Select("select v.* from tb_voucher v left join tb_seckill_voucher sv on sv.voucher_id = v.id " +
            "where v.status = 1 and (v.type = 0 or (sv.end_time is not null and sv.end_time > now())) " +
            "order by v.create_time desc")
    List<Voucher> listOnSale();

}
