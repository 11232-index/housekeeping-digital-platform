package com.smart.service.impl;

import com.smart.constant.MessageConstant;
import com.smart.constant.StatusConstant;
import com.smart.dto.EmployeeLoginDTO;
import com.smart.dto.PasswordEditDTO;
import com.smart.entity.Employee;
import com.smart.exception.AccountLockedException;
import com.smart.exception.AccountNotFoundException;
import com.smart.exception.PasswordErrorException;
import com.smart.mapper.EmployeeMapper;
import com.smart.service.EmployeeAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmployeeAuthServiceImpl implements EmployeeAuthService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 员工登录（使用employee表，BCrypt加密）
     *
     * @param employeeLoginDTO
     * @return
     */
    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        // 1、根据用户名查询employee表中的数据
        Employee employee = employeeMapper.getByUsername(username);

        // 2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 密码比对，进行BCrypt加密后再比对
        log.info("员工登录，用户名: {}", username);
        boolean matches = passwordEncoder.matches(password, employee.getPassword());
        log.info("密码验证结果: {}", matches);

        if (!matches) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        return employee;
    }

    /**
     * 修改员工密码（使用employee表，BCrypt加密）
     *
     * @param empId
     * @param passwordEditDTO
     */
    @Override
    public void editPassword(Long empId, PasswordEditDTO passwordEditDTO) {
        log.info("修改员工密码，empId: {}", empId);

        // 1、查询当前员工的密码
        Employee employee = employeeMapper.getById(empId);
        if (employee == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 2、验证原始密码是否正确
        if (!passwordEncoder.matches(passwordEditDTO.getOldPassword(), employee.getPassword())) {
            log.warn("原始密码错误，empId: {}", empId);
            throw new PasswordErrorException(MessageConstant.OLD_PASSWORD_ERROR);
        }

        // 3、BCrypt加密新密码并更新
        String encodedNewPassword = passwordEncoder.encode(passwordEditDTO.getNewPassword());
        Employee updateEmployee = Employee.builder()
                .id(empId)
                .password(encodedNewPassword)
                .build();
        employeeMapper.editPassword(updateEmployee);
        log.info("员工密码修改成功");
    }
}
