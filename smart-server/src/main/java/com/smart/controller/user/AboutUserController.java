package com.smart.controller.user;

import com.smart.dto.AboutPageQueryDTO;
import com.smart.entity.About;
import com.smart.result.PageResult;
import com.smart.result.Result;
import com.smart.service.AboutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C端 - 关于我们
 */
@RestController
@RequestMapping("/user/about")
@Slf4j
public class AboutUserController {

    @Autowired
    private AboutService aboutService;

    /**
     * 查询全部关于我们（按排序+创建时间）
     */
    @GetMapping("/list")
    public Result<List<About>> list() {
        AboutPageQueryDTO dto = new AboutPageQueryDTO();
        dto.setPage(1);
        dto.setPageSize(100);
        PageResult result = aboutService.pageQuery(dto);
        @SuppressWarnings("unchecked")
        List<About> list = result.getRecords();
        log.info("C端查询关于我们，共{}条", list.size());
        return Result.success(list);
    }

}
