package com.smart.service;

import com.smart.dto.EmployeeLoginDTO;
import com.smart.dto.PasswordEditDTO;
import com.smart.entity.Administrator;

public interface AdminService {

    /**
     * 管理员登录
     * @param employeeLoginDTO
     * @return
     */
    Administrator login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 修改管理员密码
     * @param adminId
     * @param passwordEditDTO
     */
    void editPassword(Long adminId, PasswordEditDTO passwordEditDTO);

    /**
     * 修改管理员名称
     * @param adminId
     * @param name
     */
    void editName(Long adminId, String name);

    /**
     * 设置密保
     * @param adminId
     * @param question
     * @param answer
     */
    void setSecurity(Long adminId, String question, String answer);

    /**
     * 根据用户名获取密保问题
     * @param username
     * @return 密保问题，未设置则返回null
     */
    String getSecurityQuestion(String username);

    /**
     * 重置密码（通过密保验证）
     * @param username
     * @param answer
     * @param newPassword
     */
    void resetPassword(String username, String answer, String newPassword);
}
