package com.smart.controller.user;

import com.smart.context.BaseContext;
import com.smart.entity.SearchHistory;
import com.smart.result.Result;
import com.smart.service.SearchHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端 - 搜索记录
 */
@RestController
@RequestMapping("/user/search-history")
@Slf4j
public class SearchHistoryController {

    @Autowired
    private SearchHistoryService searchHistoryService;

    /**
     * 新增搜索记录
     */
    @PostMapping
    public Result save(@RequestParam String keyword) {
        Long userId = BaseContext.getCurrentId();
        log.info("新增搜索记录，userId={}，keyword={}", userId, keyword);
        searchHistoryService.save(keyword, userId);
        return Result.success();
    }

    /**
     * 查询搜索记录列表
     */
    @GetMapping("/list")
    public Result<List<SearchHistory>> list() {
        Long userId = BaseContext.getCurrentId();
        log.info("查询搜索记录，userId={}", userId);
        List<SearchHistory> list = searchHistoryService.listByUserId(userId);
        return Result.success(list);
    }

    /**
     * 清空搜索记录
     */
    @DeleteMapping("/clean")
    public Result clean() {
        Long userId = BaseContext.getCurrentId();
        log.info("清空搜索记录，userId={}", userId);
        searchHistoryService.clean(userId);
        return Result.success();
    }

}
