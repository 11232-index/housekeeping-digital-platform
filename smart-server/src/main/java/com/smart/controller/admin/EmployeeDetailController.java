package com.smart.controller.admin;

import com.smart.annotation.RoleCheck;
import com.smart.dto.EmployeeDetailDTO;
import com.smart.result.Result;
import com.smart.service.EmployeeDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 员工详细信息管理
 */
@RestController
@RequestMapping("/admin/employee/detail")
@Slf4j
public class EmployeeDetailController {

    @Autowired
    private EmployeeDetailService employeeDetailService;

    /**
     * 根据员工ID查询详细信息
     *
     * @param employeeId
     * @return
     */
    @GetMapping("/{employeeId}")
    @RoleCheck({"admin", "manager"})
    public Result<EmployeeDetailDTO> getByEmployeeId(@PathVariable Long employeeId) {
        log.info("查询员工详细信息，employeeId：{}", employeeId);
        EmployeeDetailDTO detail = employeeDetailService.getByEmployeeId(employeeId);
        return Result.success(detail);
    }

    /**
     * 新增员工详细信息
     *
     * @param employeeDetailDTO
     * @return
     */
    @PostMapping
    @RoleCheck({"admin"})
    public Result save(@RequestBody EmployeeDetailDTO employeeDetailDTO) {
        log.info("新增员工详细信息：{}", employeeDetailDTO);
        employeeDetailService.save(employeeDetailDTO);
        return Result.success();
    }

    @PutMapping
    @RoleCheck({"admin"})
    public Result update(@RequestBody EmployeeDetailDTO employeeDetailDTO) {
        log.info("修改员工详细信息：{}", employeeDetailDTO);
        employeeDetailService.update(employeeDetailDTO);
        return Result.success();
    }

    @DeleteMapping("/{employeeId}")
    @RoleCheck({"admin"})
    public Result delete(@PathVariable Long employeeId) {
        log.info("删除员工详细信息，employeeId：{}", employeeId);
        employeeDetailService.deleteByEmployeeId(employeeId);
        return Result.success();
    }
}
