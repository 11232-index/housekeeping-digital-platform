package com.smart.service;

import com.smart.dto.EmployeeLoginDTO;
import com.smart.vo.HousekeeperWorkerLoginVO;

/**
 * 家政员小程序端认证
 */
public interface WorkerAuthService {

    /**
     * 家政员登录：账号密码须存在于 employee 表且 role = cleaner
     *
     * @param employeeLoginDTO
     * @return
     */
    HousekeeperWorkerLoginVO loginHousekeeper(EmployeeLoginDTO employeeLoginDTO);
}
