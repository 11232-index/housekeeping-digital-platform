package com.smart.controller.employee;

import com.smart.annotation.RoleCheck;
import com.smart.dto.HousekeeperScheduleDTO;
import com.smart.dto.HousekeeperSchedulePageQueryDTO;
import com.smart.entity.HousekeeperSchedule;
import com.smart.result.PageResult;
import com.smart.result.Result;
import com.smart.service.HousekeeperScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工端 - 家政员预约时间管理（仅admin和manager可见）
 */
@RestController
@RequestMapping("/employee/housekeeper-schedule")
@Slf4j
public class HousekeeperScheduleEmployeeController {

    @Autowired
    private HousekeeperScheduleService scheduleService;

    /** 新增预约时间 */
    @PostMapping
    @RoleCheck({"admin", "manager"})
    public Result save(@RequestBody HousekeeperScheduleDTO dto) {
        log.info("员工端新增家政员预约时间：{}", dto);
        scheduleService.save(dto);
        return Result.success();
    }

    /** 分页查询 */
    @GetMapping("/page")
    @RoleCheck({"admin", "manager"})
    public Result<PageResult> page(HousekeeperSchedulePageQueryDTO dto) {
        log.info("员工端家政员预约时间分页查询：{}", dto);
        return Result.success(scheduleService.pageQuery(dto));
    }

    /** 根据id查询 */
    @GetMapping("/{id}")
    @RoleCheck({"admin", "manager"})
    public Result<HousekeeperSchedule> getById(@PathVariable Long id) {
        return Result.success(scheduleService.getById(id));
    }

    /** 修改预约时间 */
    @PutMapping
    @RoleCheck({"admin", "manager"})
    public Result update(@RequestBody HousekeeperScheduleDTO dto) {
        log.info("员工端修改家政员预约时间：{}", dto);
        scheduleService.update(dto);
        return Result.success();
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    @RoleCheck({"admin", "manager"})
    public Result delete(@PathVariable Long id) {
        log.info("员工端删除家政员预约时间，id={}", id);
        scheduleService.delete(id);
        return Result.success();
    }

    /** 批量删除 */
    @DeleteMapping("/batch")
    @RoleCheck({"admin", "manager"})
    public Result batchDelete(@RequestBody List<Long> ids) {
        log.info("员工端批量删除家政员预约时间：{}", ids);
        scheduleService.deleteBatch(ids);
        return Result.success();
    }

    /** 批量更新状态 */
    @PutMapping("/batch/status/{status}")
    @RoleCheck({"admin", "manager"})
    public Result batchUpdateStatus(@RequestBody List<Long> ids, @PathVariable Integer status) {
        log.info("员工端批量更新预约时间状态：ids={}, status={}", ids, status);
        scheduleService.updateStatusBatch(ids, status);
        return Result.success();
    }

}
