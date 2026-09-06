package com.smart.controller.user;

import com.smart.context.BaseContext;
import com.smart.entity.HousekeeperSearchHistory;
import com.smart.result.Result;
import com.smart.service.HousekeeperSearchHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/housekeeper-search-history")
@Slf4j
public class HousekeeperSearchHistoryController {

    @Autowired
    private HousekeeperSearchHistoryService service;

    @PostMapping
    public Result save(@RequestParam String keyword) {
        Long userId = BaseContext.getCurrentId();
        service.save(keyword, userId);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<HousekeeperSearchHistory>> list() {
        return Result.success(service.listByUserId(BaseContext.getCurrentId()));
    }

    @DeleteMapping("/clean")
    public Result clean() {
        service.clean(BaseContext.getCurrentId());
        return Result.success();
    }

}
