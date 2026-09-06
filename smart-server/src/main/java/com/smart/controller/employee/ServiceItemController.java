package com.smart.controller.employee;

import com.smart.annotation.RoleCheck;
import com.smart.dto.ServiceItemDTO;
import com.smart.dto.ServiceItemPageQueryDTO;
import com.smart.result.PageResult;
import com.smart.result.Result;
import com.smart.service.ServiceItemService;
import com.smart.vo.ServiceItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工端 - 服务管理（CRUD）
 */
@RestController
@RequestMapping("/employee/service-item")
@Slf4j
public class ServiceItemController {

    @Autowired
    private ServiceItemService serviceItemService;

    /**
     * 新增服务项目
     *
     * @param serviceItemDTO
     * @return
     */
    @PostMapping
    @RoleCheck({"admin", "manager"})
    @CacheEvict(value = "service", key = "#serviceItemDTO.categoryId", condition = "#serviceItemDTO.categoryId != null")
    public Result save(@RequestBody ServiceItemDTO serviceItemDTO) {
        log.info("新增服务项目，服务数据：{}", serviceItemDTO);
        serviceItemService.save(serviceItemDTO);
        return Result.success();
    }

    /**
     * 分页查询
     *
     * @param serviceItemPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @Cacheable(value = "service", key = "#serviceItemPageQueryDTO.categoryId", condition = "#serviceItemPageQueryDTO.categoryId != null", sync = true)
    public Result<PageResult> page(ServiceItemPageQueryDTO serviceItemPageQueryDTO) {
        log.info("服务信息分页查询：{}", serviceItemPageQueryDTO);
        PageResult pageResult = serviceItemService.pageQuery(serviceItemPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据id查询服务信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<ServiceItemVO> getById(@PathVariable Long id) {
        log.info("根据id查询服务信息：{}", id);
        ServiceItemVO serviceItemVO = serviceItemService.getById(id);
        return Result.success(serviceItemVO);
    }

    /**
     * 修改服务信息
     *
     * @param serviceItemDTO
     * @return
     */
    @PutMapping
    @RoleCheck({"admin", "manager"})
    @CacheEvict(value = "service", key = "#serviceItemDTO.categoryId", condition = "#serviceItemDTO.categoryId != null")
    public Result update(@RequestBody ServiceItemDTO serviceItemDTO) {
        log.info("服务信息修改：{}", serviceItemDTO);
        serviceItemService.update(serviceItemDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @RoleCheck({"admin", "manager"})
    @CacheEvict(value = "service", allEntries = true)
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("启用禁用服务：status={}, id={}", status, id);
        serviceItemService.startOrStop(status, id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RoleCheck({"admin", "manager"})
    @CacheEvict(value = "service", allEntries = true)
    public Result delete(@PathVariable Long id) {
        log.info("服务信息删除：{}", id);
        serviceItemService.delete(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @RoleCheck({"admin", "manager"})
    @CacheEvict(value = "service", allEntries = true)
    public Result batchDelete(@RequestBody List<Long> ids) {
        log.info("服务信息批量删除：{}", ids);
        serviceItemService.batchDelete(ids);
        return Result.success();
    }
}
