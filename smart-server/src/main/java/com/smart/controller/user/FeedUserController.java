package com.smart.controller.user;

import com.smart.entity.Feed;
import com.smart.result.Result;
import com.smart.service.FeedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** C端 - 资讯中心 */
@RestController
@RequestMapping("/user/feed")
@Slf4j
public class FeedUserController {

    @Autowired
    private FeedService feedService;

    /** 查询feed列表（首页资讯中心） */
    @GetMapping("/list")
    public Result<List<Feed>> list() {
        List<Feed> list = feedService.list(20);
        log.info("C端查询资讯中心，共{}条", list.size());
        return Result.success(list);
    }

}
