package com.smart.controller.admin;

import com.smart.dto.HotPicksDTO;
import com.smart.dto.HotPicksPageQueryDTO;
import com.smart.entity.HotPicks;
import com.smart.result.PageResult;
import com.smart.result.Result;
import com.smart.service.HotPicksService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 服务推荐管理（CRUD）
 */
@RestController
@RequestMapping("/admin/hot-picks")
@Slf4j
public class HotPicksController {

    @Autowired
    private HotPicksService hotPicksService;

    @PostMapping
    public Result save(@RequestBody HotPicksDTO hotPicksDTO) {
        log.info("新增服务推荐：{}", hotPicksDTO);
        hotPicksService.save(hotPicksDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(HotPicksPageQueryDTO dto) {
        log.info("服务推荐分页查询：{}", dto);
        PageResult pageResult = hotPicksService.pageQuery(dto);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<HotPicks> getById(@PathVariable Long id) {
        log.info("根据id查询服务推荐：{}", id);
        HotPicks hotPicks = hotPicksService.getById(id);
        return Result.success(hotPicks);
    }

    @PutMapping
    public Result update(@RequestBody HotPicksDTO hotPicksDTO) {
        log.info("服务推荐修改：{}", hotPicksDTO);
        hotPicksService.update(hotPicksDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("启用/禁用服务推荐：status={}, id={}", status, id);
        hotPicksService.startOrStop(status, id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        log.info("服务推荐删除：{}", id);
        hotPicksService.delete(id);
        return Result.success();
    }

}
