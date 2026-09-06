package com.smart.service.impl;

import com.smart.constant.MessageConstant;
import com.smart.constant.StatusConstant;
import com.smart.dto.EmployeeLoginDTO;
import com.smart.dto.PasswordEditDTO;
import com.smart.entity.Administrator;
import com.smart.exception.AccountLockedException;
import com.smart.exception.AccountNotFoundException;
import com.smart.exception.PasswordErrorException;
import com.smart.mapper.AdministratorMapper;
import com.smart.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdministratorMapper administratorMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 管理员登录（使用administrator表，BCrypt加密）
     *
     * @param employeeLoginDTO
     * @return
     */
    @Override
    public Administrator login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询administrator表中的数据
        Administrator administrator = administratorMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (administrator == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对，进行BCrypt加密后再比对
        log.info("输入的密码: {}", password);
        log.info("数据库中的密码哈希: {}", administrator.getPassword());
        boolean matches = passwordEncoder.matches(password, administrator.getPassword());
        log.info("密码验证结果: {}", matches);

        if (!matches) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (administrator.getStatus() == StatusConstant.DISABLE) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        return administrator;
    }

    /**
     * 修改管理员密码（使用administrator表，BCrypt加密）
     *
     * @param adminId
     * @param passwordEditDTO
     */
    @Override
    public void editPassword(Long adminId, PasswordEditDTO passwordEditDTO) {
        log.info("修改管理员密码，adminId: {}", adminId);

        // 1. 查询当前管理员的密码
        Administrator administrator = administratorMapper.getById(adminId);
        if (administrator == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 2. 验证原始密码是否正确
        if (!passwordEncoder.matches(passwordEditDTO.getOldPassword(), administrator.getPassword())) {
            log.warn("原始密码错误，adminId: {}", adminId);
            throw new PasswordErrorException(MessageConstant.OLD_PASSWORD_ERROR);
        }

        // 3. BCrypt加密新密码并更新
        String encodedNewPassword = passwordEncoder.encode(passwordEditDTO.getNewPassword());
        Administrator updateAdmin = Administrator.builder()
                .id(adminId)
                .password(encodedNewPassword)
                .build();
        administratorMapper.editPassword(updateAdmin);
        log.info("管理员密码修改成功");
    }

    /**
     * 修改管理员姓名
     * @param adminId
     * @param name
     * @return
     */
    @Override
    public void editName(Long adminId, String name) {
        Administrator updateAdmin = Administrator.builder()
                .id(adminId)
                .name(name)
                .build();
                administratorMapper.editName(updateAdmin);
                log.info("管理员姓名修改成功");
                return;
    }

    /**
     * 设置密保（答案BCrypt加密存储）
     */
    @Override
    public void setSecurity(Long adminId, String question, String answer) {
        Administrator updateAdmin = Administrator.builder()
                .id(adminId)
                .securityQuestion(question)
                .securityAnswer(passwordEncoder.encode(answer))
                .build();
        administratorMapper.updateSecurity(updateAdmin);
        log.info("管理员密保设置成功");
    }

    /**
     * 根据用户名获取密保问题
     */
    @Override
    public String getSecurityQuestion(String username) {
        Administrator administrator = administratorMapper.getByUsername(username);
        if (administrator == null || administrator.getSecurityQuestion() == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        return administrator.getSecurityQuestion();
    }

    /**
     * 通过密保验证后重置密码
     */
    @Override
    public void resetPassword(String username, String answer, String newPassword) {
        Administrator administrator = administratorMapper.getByUsername(username);
        if (administrator == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        // 验证密保答案
        if (administrator.getSecurityAnswer() == null
                || !passwordEncoder.matches(answer, administrator.getSecurityAnswer())) {
            throw new PasswordErrorException("密保答案错误");
        }
        // 加密新密码并更新
        Administrator updateAdmin = Administrator.builder()
                .id(administrator.getId())
                .password(passwordEncoder.encode(newPassword))
                .build();
        administratorMapper.editPassword(updateAdmin);
        log.info("管理员密码重置成功");
    }
}
