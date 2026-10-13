package com.smart.controller.user;

import com.smart.context.BaseContext;
import com.smart.dto.VoucherSeckillDTO;
import com.smart.result.Result;
import com.smart.service.VoucherService;
import com.smart.vo.UserVoucherVO;
import com.smart.vo.VoucherVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端 - 优惠券接口
 */
@RestController
@RequestMapping("/user/voucher")
@Slf4j
public class VoucherUserController {

    @Autowired
    private VoucherService voucherService;

    /**
     * 查询上架中的优惠券列表（含秒杀信息）
     */
    @GetMapping("/list")
    public Result<List<VoucherVO>> list() {
        List<VoucherVO> list = voucherService.listOnSale();
        log.info("C端查询上架优惠券，共{}条", list.size());
        return Result.success(list);
    }

    /**
     * 领取/秒杀优惠券
     */
    @PostMapping("/seckill")
    public Result<VoucherVO> seckill(@RequestBody VoucherSeckillDTO voucherSeckillDTO) {
        log.info("用户领取/秒杀优惠券：{}", voucherSeckillDTO);
        VoucherVO voucherVO = voucherService.seckillVoucher(voucherSeckillDTO.getVoucherId());
        return Result.success(voucherVO);
    }

    /**
     * 我的优惠券（含已使用，按领取时间倒序）
     */
    @GetMapping("/mine")
    public Result<List<UserVoucherVO>> mine() {
        Long userId = BaseContext.getCurrentId();
        List<UserVoucherVO> list = voucherService.listMyVouchers(userId);
        return Result.success(list);
    }

}
