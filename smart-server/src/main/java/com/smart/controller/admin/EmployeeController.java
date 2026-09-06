package com.smart.controller.admin;

import com.smart.annotation.RoleCheck;
import com.smart.dto.EmployeeDTO;
import com.smart.dto.EmployeePageQueryDTO;
import com.smart.entity.Employee;
import com.smart.result.PageResult;
import com.smart.result.Result;
import com.smart.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 员工管理（CRUD）—— 仅 admin 可操作
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 新增员工信息
     *
     * @param employeeDTO
     * @return
     */
    @PostMapping
    @RoleCheck({"admin"})
    public Result save(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工，员工数据：{}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     * 分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @RoleCheck({"admin"})
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("员工信息分页查询：{}", employeePageQueryDTO);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 启用禁用员工账号
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @RoleCheck({"admin"})
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("启用禁用员工账号：{}", status);
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 根据id查询员工信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @RoleCheck({"admin", "manager"})
    public Result<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询员工信息：{}", id);
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    /**
     * 修改员工信息
     *
     * @param employeeDTO
     * @return
     */
    @PutMapping
    @RoleCheck({"admin"})
    public Result update(@RequestBody EmployeeDTO employeeDTO) {
        log.info("员工信息修改：{}", employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }

    /**
     * 删除员工信息
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @RoleCheck({"admin"})
    public Result delete(@PathVariable Long id) {
        log.info("员工信息删除：{}", id);
        employeeService.delete(id);
        return Result.success();
    }
}
