package com.smart.controller.admin;

import com.smart.dto.AboutDTO;
import com.smart.dto.AboutPageQueryDTO;
import com.smart.entity.About;
import com.smart.result.PageResult;
import com.smart.result.Result;
import com.smart.service.AboutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 关于我们管理（CRUD）
 */
@RestController
@RequestMapping("/admin/about")
@Slf4j
public class AboutController {

    @Autowired
    private AboutService aboutService;

    @PostMapping
    public Result save(@RequestBody AboutDTO aboutDTO) {
        log.info("新增关于我们：{}", aboutDTO);
        aboutService.save(aboutDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(AboutPageQueryDTO dto) {
        log.info("关于我们分页查询：{}", dto);
        PageResult pageResult = aboutService.pageQuery(dto);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<About> getById(@PathVariable Long id) {
        log.info("根据id查询关于我们：{}", id);
        About about = aboutService.getById(id);
        return Result.success(about);
    }

    @PutMapping
    public Result update(@RequestBody AboutDTO aboutDTO) {
        log.info("关于我们修改：{}", aboutDTO);
        aboutService.update(aboutDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        log.info("关于我们删除：{}", id);
        aboutService.delete(id);
        return Result.success();
    }

}
