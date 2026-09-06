package com.smart.service;

import com.smart.dto.EmployeeLoginDTO;
import com.smart.dto.PasswordEditDTO;
import com.smart.entity.Employee;

public interface EmployeeAuthService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 修改员工密码
     * @param empId
     * @param passwordEditDTO
     */
    void editPassword(Long empId, PasswordEditDTO passwordEditDTO);
}
