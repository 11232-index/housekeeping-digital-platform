package com.smart.controller.admin;

import com.smart.dto.HousekeeperDTO;
import com.smart.dto.HousekeeperPageQueryDTO;
import com.smart.entity.Housekeeper;
import com.smart.result.PageResult;
import com.smart.result.Result;
import com.smart.service.HousekeeperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 家政员管理
 */
@RestController
@RequestMapping("/admin/housekeeper")
@Slf4j
public class HousekeeperController {

    @Autowired
    private HousekeeperService housekeeperService;

    @Autowired
    private CacheManager cacheManager;

    /** 分页查询家政员 */
    @GetMapping("/page")
    public Result<PageResult> page(HousekeeperPageQueryDTO dto) {
        log.info("家政员分页查询：{}", dto);
        return Result.success(housekeeperService.pageQuery(dto));
    }

    /** 根据id查询 */
    @GetMapping("/{id}")
    public Result<Housekeeper> getById(@PathVariable Long id) {
        return Result.success(housekeeperService.getById(id));
    }

    /** 修改家政员 */
    @PutMapping
    public Result update(@RequestBody HousekeeperDTO dto) {
        log.info("修改家政员：{}", dto);
        housekeeperService.update(dto);
        // 手动清缓存，确保C端查询到最新数据
        if (cacheManager.getCache("housekeeper") != null) {
            cacheManager.getCache("housekeeper").clear();
            log.info("已清空housekeeper缓存");
        }
        return Result.success();
    }

}
