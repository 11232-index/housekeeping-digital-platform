package com.smart.service;

import com.smart.dto.VoucherDTO;
import com.smart.dto.VoucherPageQueryDTO;
import com.smart.result.PageResult;
import com.smart.vo.UserVoucherVO;
import com.smart.vo.VoucherVO;

import java.util.List;

/**
 * 优惠券服务接口
 */
public interface VoucherService {

    /**
     * 新增优惠券（id 使用 RedisIdWorker 全局ID生成器生成）
     *
     * @param dto 优惠券参数
     */
    void save(VoucherDTO dto);

    /**
     * 分页查询优惠券
     *
     * @param dto 分页查询参数
     * @return 分页结果
     */
    PageResult pageQuery(VoucherPageQueryDTO dto);

    /**
     * 根据id查询优惠券（含秒杀信息）
     *
     * @param id 优惠券id
     * @return 优惠券VO
     */
    VoucherVO getById(Long id);

    /**
     * 修改优惠券
     *
     * @param dto 优惠券参数
     */
    void update(VoucherDTO dto);

    /**
     * 上架/下架/过期（status：1上架；2下架；3过期）
     *
     * @param status 目标状态
     * @param id     优惠券id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 删除优惠券
     *
     * @param id 优惠券id
     */
    void delete(Long id);

    /**
     * 查询上架中的优惠券（C端展示，含秒杀信息）
     *
     * @return 优惠券列表
     */
    List<VoucherVO> listOnSale();

    /**
     * 领取/秒杀优惠券（秒杀券扣减库存、一人一单）
     *
     * @param voucherId 优惠券id
     * @return 领取成功的优惠券信息
     */
    VoucherVO seckillVoucher(Long voucherId);

    /**
     * 查询某用户的全部优惠券（含已使用，按领取时间倒序）—— C端「我的优惠券」
     *
     * @param userId 用户id
     * @return 用户的优惠券列表
     */
    List<UserVoucherVO> listMyVouchers(Long userId);

}
