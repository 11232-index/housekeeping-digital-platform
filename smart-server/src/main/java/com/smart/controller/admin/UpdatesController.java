package com.smart.controller.admin;

import com.smart.dto.UpdatesDTO;
import com.smart.dto.UpdatesPageQueryDTO;
import com.smart.entity.Updates;
import com.smart.result.PageResult;
import com.smart.result.Result;
import com.smart.service.UpdatesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 动态消息管理（CRUD）
 */
@RestController
@RequestMapping("/admin/updates")
@Slf4j
public class UpdatesController {

    @Autowired
    private UpdatesService updatesService;

    /**
     * 新增动态消息
     * @param updatesDTO
     * @return
     */
    @PostMapping
    public Result save(@RequestBody UpdatesDTO updatesDTO) {
        log.info("新增动态消息：{}", updatesDTO);
        updatesService.save(updatesDTO);
        return Result.success();
    }

    /**
     * 分页查询动态消息
     * @param updatesPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> page(UpdatesPageQueryDTO updatesPageQueryDTO) {
        log.info("动态消息分页查询：{}", updatesPageQueryDTO);
        PageResult pageResult = updatesService.pageQuery(updatesPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据id查询动态消息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Updates> getById(@PathVariable Long id) {
        log.info("根据id查询动态消息：{}", id);
        Updates updates = updatesService.getById(id);
        return Result.success(updates);
    }

    /**
     * 修改动态消息
     * @param updatesDTO
     * @return
     */
    @PutMapping
    public Result update(@RequestBody UpdatesDTO updatesDTO) {
        log.info("动态消息修改：{}", updatesDTO);
        updatesService.update(updatesDTO);
        return Result.success();
    }

    /**
     * 删除动态消息
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        log.info("动态消息删除：{}", id);
        updatesService.delete(id);
        return Result.success();
    }

}
