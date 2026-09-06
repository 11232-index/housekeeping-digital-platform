package com.smart.controller.user;

import com.smart.dto.UpdatesPageQueryDTO;
import com.smart.entity.Updates;
import com.smart.result.PageResult;
import com.smart.result.Result;
import com.smart.service.UpdatesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * C端 - 最新动态
 */
@RestController
@RequestMapping("/user/updates")
@Slf4j
public class UpdatesUserController {

    @Autowired
    private UpdatesService updatesService;

    /**
     * 分页查询最新动态
     */
    @GetMapping("/page")
    public Result<PageResult> page(UpdatesPageQueryDTO dto) {
        log.info("C端查询最新动态：{}", dto);
        PageResult pageResult = updatesService.pageQuery(dto);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询最新动态详情
     */
    @GetMapping("/{id}")
    public Result<Updates> getById(@PathVariable Long id) {
        log.info("C端查询最新动态详情：id={}", id);
        Updates updates = updatesService.getById(id);
        return Result.success(updates);
    }

}
