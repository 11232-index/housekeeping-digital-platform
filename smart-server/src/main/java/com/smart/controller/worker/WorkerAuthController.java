package com.smart.controller.worker;

import com.smart.dto.EmployeeLoginDTO;
import com.smart.result.Result;
import com.smart.service.WorkerAuthService;
import com.smart.vo.HousekeeperWorkerLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 家政员小程序端认证（公开接口，不挂在 /employee/** 下以避免被员工端拦截器拦截）
 */
@RestController
@RequestMapping("/worker/auth")
@Slf4j
public class WorkerAuthController {

    @Autowired
    private WorkerAuthService workerAuthService;

    /**
     * 家政员登录（账号密码须存在于 employee 表且 role = cleaner）
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<HousekeeperWorkerLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("家政员小程序端登录：{}", employeeLoginDTO);
        return Result.success(workerAuthService.loginHousekeeper(employeeLoginDTO));
    }
}
