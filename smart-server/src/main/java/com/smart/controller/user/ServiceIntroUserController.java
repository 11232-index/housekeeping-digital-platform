package com.smart.controller.user;

import com.smart.dto.ServiceIntroPageQueryDTO;
import com.smart.entity.ServiceIntro;
import com.smart.result.PageResult;
import com.smart.result.Result;
import com.smart.service.ServiceIntroService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * C端 - 服务介绍
 */
@RestController
@RequestMapping("/user/service-intro")
@Slf4j
public class ServiceIntroUserController {

    @Autowired
    private ServiceIntroService serviceIntroService;

    @GetMapping("/page")
    public Result<PageResult> page(ServiceIntroPageQueryDTO dto) {
        log.info("C端查询服务介绍：{}", dto);
        PageResult pageResult = serviceIntroService.pageQuery(dto);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<ServiceIntro> getById(@PathVariable Long id) {
        log.info("C端查询服务介绍详情：id={}", id);
        ServiceIntro serviceIntro = serviceIntroService.getById(id);
        return Result.success(serviceIntro);
    }

}
