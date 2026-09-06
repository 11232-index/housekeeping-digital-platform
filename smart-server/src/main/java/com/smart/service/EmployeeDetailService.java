package com.smart.service;

import com.smart.dto.EmployeeDetailDTO;

public interface EmployeeDetailService {

    /**
     * 根据员工ID查询详细信息
     *
     * @param employeeId
     * @return
     */
    EmployeeDetailDTO getByEmployeeId(Long employeeId);

    /**
     * 新增员工详细信息
     *
     * @param employeeDetailDTO
     */
    void save(EmployeeDetailDTO employeeDetailDTO);

    /**
     * 修改员工详细信息
     *
     * @param employeeDetailDTO
     */
    void update(EmployeeDetailDTO employeeDetailDTO);

    /**
     * 根据员工ID删除详细信息
     *
     * @param employeeId
     */
    void deleteByEmployeeId(Long employeeId);
}
