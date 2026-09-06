package com.smart.controller.admin;

import com.smart.dto.ServiceIntroDTO;
import com.smart.dto.ServiceIntroPageQueryDTO;
import com.smart.entity.ServiceIntro;
import com.smart.result.PageResult;
import com.smart.result.Result;
import com.smart.service.ServiceIntroService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 服务介绍管理（CRUD）
 */
@RestController
@RequestMapping("/admin/service-intro")
@Slf4j
public class ServiceIntroController {

    @Autowired
    private ServiceIntroService serviceIntroService;

    /**
     * 新增服务介绍
     */
    @PostMapping
    public Result save(@RequestBody ServiceIntroDTO serviceIntroDTO) {
        log.info("新增服务介绍：{}", serviceIntroDTO);
        serviceIntroService.save(serviceIntroDTO);
        return Result.success();
    }

    /**
     * 分页查询服务介绍
     */
    @GetMapping("/page")
    public Result<PageResult> page(ServiceIntroPageQueryDTO dto) {
        log.info("服务介绍分页查询：{}", dto);
        PageResult pageResult = serviceIntroService.pageQuery(dto);
        return Result.success(pageResult);
    }

    /**
     * 根据id查询服务介绍
     */
    @GetMapping("/{id}")
    public Result<ServiceIntro> getById(@PathVariable Long id) {
        log.info("根据id查询服务介绍：{}", id);
        ServiceIntro serviceIntro = serviceIntroService.getById(id);
        return Result.success(serviceIntro);
    }

    /**
     * 修改服务介绍
     */
    @PutMapping
    public Result update(@RequestBody ServiceIntroDTO serviceIntroDTO) {
        log.info("服务介绍修改：{}", serviceIntroDTO);
        serviceIntroService.update(serviceIntroDTO);
        return Result.success();
    }

    /**
     * 删除服务介绍
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        log.info("服务介绍删除：{}", id);
        serviceIntroService.delete(id);
        return Result.success();
    }

}
