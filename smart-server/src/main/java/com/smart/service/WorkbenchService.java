package com.smart.service;

import com.smart.vo.WorkbenchOverviewVO;

/**
 * 员工端工作台服务
 */
public interface WorkbenchService {

    /**
     * 员工端工作台聚合数据（待办 + 当日指标 + 当日预约时段）
     * @param date 统计日期（yyyy-MM-dd，不传默认今天）
     * @return 工作台聚合数据
     */
    WorkbenchOverviewVO getOverview(String date);

}
