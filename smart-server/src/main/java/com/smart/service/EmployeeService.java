package com.smart.service;

import com.smart.dto.EmployeeDTO;
import com.smart.dto.EmployeePageQueryDTO;
import com.smart.entity.Employee;
import com.smart.result.PageResult;

public interface EmployeeService {

    /**
     * 新增员工
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据ID查询员工信息
     * @param id
     * @return
     */
    Employee getById(Long id);

    /**
     * 修改员工信息
     * @param employeeDTO
     */
    void update(EmployeeDTO employeeDTO);

    /**
     * 删除员工信息
     * @param id
     */
    void delete(Long id);
}
