package com.smart.controller.user;

import com.smart.entity.HotPicks;
import com.smart.mapper.HotPicksMapper;
import com.smart.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C端 - 服务推荐轮播图
 */
@RestController
@RequestMapping("/user/hot-picks")
@Slf4j
public class HotPicksUserController {

    @Autowired
    private HotPicksMapper hotPicksMapper;

    /**
     * 查询启用的服务推荐（最多5条，按排序+创建时间）
     */
    @GetMapping("/list")
    public Result<List<HotPicks>> list() {
        List<HotPicks> list = hotPicksMapper.listEnabled();
        log.info("C端查询服务推荐，共{}条", list.size());
        return Result.success(list);
    }

}
