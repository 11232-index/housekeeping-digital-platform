package com.smart.controller.user;

import com.smart.context.BaseContext;
import com.smart.entity.ServiceIntroSearchHistory;
import com.smart.result.Result;
import com.smart.service.ServiceIntroSearchHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/service-intro-search-history")
@Slf4j
public class ServiceIntroSearchHistoryController {

    @Autowired
    private ServiceIntroSearchHistoryService service;

    @PostMapping
    public Result save(@RequestParam String keyword) {
        Long userId = BaseContext.getCurrentId();
        service.save(keyword, userId);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<ServiceIntroSearchHistory>> list() {
        Long userId = BaseContext.getCurrentId();
        return Result.success(service.listByUserId(userId));
    }

    @DeleteMapping("/clean")
    public Result clean() {
        Long userId = BaseContext.getCurrentId();
        service.clean(userId);
        return Result.success();
    }

}
