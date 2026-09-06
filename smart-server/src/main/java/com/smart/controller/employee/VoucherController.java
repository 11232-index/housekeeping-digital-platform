package com.smart.controller.employee;

import com.smart.annotation.RoleCheck;
import com.smart.dto.VoucherDTO;
import com.smart.dto.VoucherPageQueryDTO;
import com.smart.result.PageResult;
import com.smart.result.Result;
import com.smart.service.VoucherService;
import com.smart.vo.VoucherVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 员工端 - 优惠券管理（CRUD）
 */
@RestController
@RequestMapping("/employee/voucher")
@Slf4j
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    /**
     * 新增优惠券（可设置类型：普通券/秒杀券）
     */
    @PostMapping
    @RoleCheck({"admin", "manager"})
    public Result save(@RequestBody VoucherDTO voucherDTO) {
        log.info("新增优惠券：{}", voucherDTO);
        voucherService.save(voucherDTO);
        return Result.success();
    }

    /**
     * 分页查询优惠券
     */
    @GetMapping("/page")
    public Result<PageResult> page(VoucherPageQueryDTO dto) {
        log.info("优惠券分页查询：{}", dto);
        PageResult pageResult = voucherService.pageQuery(dto);
        return Result.success(pageResult);
    }

    /**
     * 根据id查询优惠券（含秒杀信息）
     */
    @GetMapping("/{id}")
    public Result<VoucherVO> getById(@PathVariable Long id) {
        log.info("根据id查询优惠券：{}", id);
        VoucherVO voucherVO = voucherService.getById(id);
        return Result.success(voucherVO);
    }

    /**
     * 修改优惠券（可修改起止时间、库存、status等）
     */
    @PutMapping
    @RoleCheck({"admin", "manager"})
    public Result update(@RequestBody VoucherDTO voucherDTO) {
        log.info("优惠券修改：{}", voucherDTO);
        voucherService.update(voucherDTO);
        return Result.success();
    }

    /**
     * 上架/下架/过期（status：1上架；2下架；3过期）
     */
    @PostMapping("/status/{status}")
    @RoleCheck({"admin", "manager"})
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("上架/下架/过期优惠券：status={}, id={}", status, id);
        voucherService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 删除优惠券
     */
    @DeleteMapping("/{id}")
    @RoleCheck({"admin", "manager"})
    public Result delete(@PathVariable Long id) {
        log.info("优惠券删除：{}", id);
        voucherService.delete(id);
        return Result.success();
    }

}
