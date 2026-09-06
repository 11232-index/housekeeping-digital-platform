package com.smart.controller.employee;

import com.smart.annotation.RoleCheck;
import com.smart.dto.ServicePackageDTO;
import com.smart.dto.ServicePackagePageQueryDTO;
import com.smart.result.PageResult;
import com.smart.result.Result;
import com.smart.service.ServicePackageService;
import com.smart.vo.ServicePackageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工端 - 套餐管理（CRUD）
 */
@RestController
@RequestMapping("/employee/service-package")
@Slf4j
public class ServicePackageController {

    @Autowired
    private ServicePackageService servicePackageService;

    /**
     * 新增服务套餐
     *
     * @param servicePackageDTO
     * @return
     */
    @PostMapping
    @RoleCheck({"admin", "manager"})
    @CacheEvict(value = "servicePackage", key = "#servicePackageDTO.categoryId", condition = "#servicePackageDTO.categoryId != null")
    public Result save(@RequestBody ServicePackageDTO servicePackageDTO) {
        log.info("新增服务套餐，套餐数据：{}", servicePackageDTO);
        servicePackageService.save(servicePackageDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @Cacheable(value = "servicePackage", key = "#servicePackagePageQueryDTO.categoryId", condition = "#servicePackagePageQueryDTO.categoryId != null", sync = true)
    public Result<PageResult> page(ServicePackagePageQueryDTO servicePackagePageQueryDTO) {
        log.info("套餐信息分页查询：{}", servicePackagePageQueryDTO);
        PageResult pageResult = servicePackageService.pageQuery(servicePackagePageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<ServicePackageVO> getById(@PathVariable Long id) {
        log.info("根据id查询套餐信息：{}", id);
        ServicePackageVO servicePackageVO = servicePackageService.getById(id);
        return Result.success(servicePackageVO);
    }

    @PutMapping
    @RoleCheck({"admin", "manager"})
    @CacheEvict(value = "servicePackage", key = "#servicePackageDTO.categoryId", condition = "#servicePackageDTO.categoryId != null")
    public Result update(@RequestBody ServicePackageDTO servicePackageDTO) {
        log.info("套餐信息修改：{}", servicePackageDTO);
        servicePackageService.update(servicePackageDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @RoleCheck({"admin", "manager"})
    @CacheEvict(value = "servicePackage", allEntries = true)
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("启用禁用套餐：status={}, id={}", status, id);
        servicePackageService.startOrStop(status, id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RoleCheck({"admin", "manager"})
    @CacheEvict(value = "servicePackage", allEntries = true)
    public Result delete(@PathVariable Long id) {
        log.info("套餐信息删除：{}", id);
        servicePackageService.delete(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @RoleCheck({"admin", "manager"})
    @CacheEvict(value = "servicePackage", allEntries = true)
    public Result batchDelete(@RequestBody List<Long> ids) {
        log.info("套餐信息批量删除：{}", ids);
        servicePackageService.batchDelete(ids);
        return Result.success();
    }
}
