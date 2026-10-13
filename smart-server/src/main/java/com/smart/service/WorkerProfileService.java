package com.smart.service;

import com.smart.vo.WorkerProfileVO;

/**
 * 家政员端"我的资料"服务
 */
public interface WorkerProfileService {

    /**
     * 查询家政员本人的资料（员工基本信息 + 详细信息 + 家政员信息 + 排班 + 可预约时段）
     *
     * @param employeeId 当前登录家政员的员工id（取自令牌）
     * @return 聚合视图
     */
    WorkerProfileVO getProfile(Long employeeId);
}
