package com.smart.controller.user;

import com.smart.context.BaseContext;
import com.smart.entity.UpdatesSearchHistory;
import com.smart.result.Result;
import com.smart.service.UpdatesSearchHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端 - 最新动态搜索记录
 */
@RestController
@RequestMapping("/user/updates-search-history")
@Slf4j
public class UpdatesSearchHistoryController {

    @Autowired
    private UpdatesSearchHistoryService service;

    @PostMapping
    public Result save(@RequestParam String keyword) {
        Long userId = BaseContext.getCurrentId();
        log.info("新增最新动态搜索记录，userId={}，keyword={}", userId, keyword);
        service.save(keyword, userId);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<UpdatesSearchHistory>> list() {
        Long userId = BaseContext.getCurrentId();
        List<UpdatesSearchHistory> list = service.listByUserId(userId);
        return Result.success(list);
    }

    @DeleteMapping("/clean")
    public Result clean() {
        Long userId = BaseContext.getCurrentId();
        service.clean(userId);
        return Result.success();
    }

}
