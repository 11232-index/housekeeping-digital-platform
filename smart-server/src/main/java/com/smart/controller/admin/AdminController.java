package com.smart.controller.admin;

import com.smart.constant.JwtClaimsConstant;
import com.smart.dto.EmployeeLoginDTO;
import com.smart.dto.PasswordEditDTO;
import com.smart.entity.Administrator;
import com.smart.properties.JwtProperties;
import com.smart.result.Result;
import com.smart.service.AdminService;
import com.smart.utils.JwtUtil;
import com.smart.vo.EmployeeLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员认证（登录/登出/改密）
 */
@RestController
@RequestMapping("/admin/auth")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 管理员登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("管理员登录：{}", employeeLoginDTO);

        Administrator administrator = adminService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, administrator.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(administrator.getId())
                .userName(administrator.getUsername())
                .name(administrator.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 管理员退出
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 修改管理员密码
     *
     * @param passwordEditDTO
     * @return
     */
    @PutMapping("/editPassword")
    public Result editPassword(@RequestBody PasswordEditDTO passwordEditDTO) {
        log.info("管理员修改密码：{}", passwordEditDTO);

        // 从ThreadLocal获取当前登录管理员的ID
        Long adminId = com.smart.context.BaseContext.getCurrentId();
        log.info("当前登录管理员ID：{}", adminId);

        adminService.editPassword(adminId, passwordEditDTO);
        return Result.success();
    }

    /**
     * 修改管理员姓名
     * @param name
     * @return
     */
    @PutMapping("/editName")
    public Result editName(@RequestBody String name) {
        log.info("管理员修改姓名：{}", name);
        // 从ThreadLocal获取当前登录管理员的ID
        Long adminId = com.smart.context.BaseContext.getCurrentId();
        log.info("当前登录管理员ID：{}", adminId);
        adminService.editName(adminId, name);
        return Result.success();
    }

    /**
     * 设置密保（登录后）
     * @param body
     * @return
     */
    @PutMapping("/security")
    public Result setSecurity(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        String answer = body.get("answer");
        log.info("管理员设置密保：question={}", question);
        Long adminId = com.smart.context.BaseContext.getCurrentId();
        adminService.setSecurity(adminId, question, answer);
        return Result.success();
    }

    /**
     * 获取密保问题（忘记密码时，无需登录）
     * @param username
     * @return
     */
    @GetMapping("/securityQuestion")
    public Result<String> getSecurityQuestion(@RequestParam String username) {
        log.info("获取密保问题：username={}", username);
        String question = adminService.getSecurityQuestion(username);
        return Result.success(question);
    }

    /**
     * 重置密码（忘记密码时，通过密保验证，无需登录）
     * @param body
     * @return
     */
    @PostMapping("/resetPassword")
    public Result resetPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String answer = body.get("answer");
        String newPassword = body.get("newPassword");
        log.info("重置管理员密码：username={}", username);
        adminService.resetPassword(username, answer, newPassword);
        return Result.success();
    }
}
