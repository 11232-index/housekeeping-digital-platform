package com.smart.service.impl;

import com.smart.constant.JwtClaimsConstant;
import com.smart.dto.EmployeeLoginDTO;
import com.smart.entity.Employee;
import com.smart.entity.Housekeeper;
import com.smart.exception.LoginFailedException;
import com.smart.mapper.HousekeeperMapper;
import com.smart.properties.JwtProperties;
import com.smart.service.EmployeeAuthService;
import com.smart.service.WorkerAuthService;
import com.smart.utils.JwtUtil;
import com.smart.vo.HousekeeperWorkerLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 家政员小程序端认证实现
 */
@Slf4j
@Service
public class WorkerAuthServiceImpl implements WorkerAuthService {

    /** 家政员角色标识（employee.role，见 EmployeeServiceImpl） */
    private static final String ROLE_CLEANER = "cleaner";

    @Autowired
    private EmployeeAuthService employeeAuthService;

    @Autowired
    private HousekeeperMapper housekeeperMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public HousekeeperWorkerLoginVO loginHousekeeper(EmployeeLoginDTO employeeLoginDTO) {
        log.info("家政员登录：username={}", employeeLoginDTO.getUsername());

        // 1. 复用员工登录校验：账号存在、BCrypt密码匹配、账号未禁用；失败会自动抛对应业务异常
        Employee employee = employeeAuthService.login(employeeLoginDTO);

        // 2. 角色必须是家政员
        if (!ROLE_CLEANER.equals(employee.getRole())) {
            throw new LoginFailedException("该账号不是家政员，无法登录家政员端");
        }

        // 3. 关联 housekeeper 表取姓名、头像
        Housekeeper housekeeper = housekeeperMapper.getByEmployeeId(employee.getId());
        String name = (housekeeper != null && StringUtils.hasText(housekeeper.getName()))
                ? housekeeper.getName() : employee.getName();
        String picture = (housekeeper != null) ? housekeeper.getPicture() : null;
        log.info("家政员身份确认：employeeId={}, housekeeperId={}, name={}", employee.getId(),
                housekeeper == null ? null : housekeeper.getId(), name);

        // 4. 签发员工令牌（与 /employee/auth/login 同款：claims 存 empId）
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getEmployeeSecretKey(),
                jwtProperties.getEmployeeTtl(),
                claims);

        return HousekeeperWorkerLoginVO.builder()
                .employeeId(employee.getId())
                .housekeeperId(housekeeper == null ? null : housekeeper.getId())
                .name(name)
                .picture(picture)
                .token(token)
                .build();
    }
}
