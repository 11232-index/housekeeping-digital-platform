package com.smart.controller.employee;

import com.smart.constant.JwtClaimsConstant;
import com.smart.context.BaseContext;
import com.smart.dto.EmployeeLoginDTO;
import com.smart.dto.PasswordEditDTO;
import com.smart.entity.Employee;
import com.smart.properties.JwtProperties;
import com.smart.result.Result;
import com.smart.service.EmployeeAuthService;
import com.smart.utils.JwtUtil;
import com.smart.vo.EmployeeLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工端认证（登录/登出/改密）
 */
@RestController
@RequestMapping("/employee/auth")
@Slf4j
public class EmployeeAuthController {

    @Autowired
    private EmployeeAuthService employeeAuthService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeAuthService.login(employeeLoginDTO);

        // 登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getEmployeeSecretKey(),
                jwtProperties.getEmployeeTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .role(employee.getRole())
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 员工退出
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 修改员工密码
     *
     * @param passwordEditDTO
     * @return
     */
    @PutMapping("/editPassword")
    public Result editPassword(@RequestBody PasswordEditDTO passwordEditDTO) {
        log.info("员工修改密码：{}", passwordEditDTO);

        // 从ThreadLocal获取当前登录员工的ID
        Long empId = BaseContext.getCurrentId();
        log.info("当前登录员工ID：{}", empId);

        employeeAuthService.editPassword(empId, passwordEditDTO);
        return Result.success();
    }
}
