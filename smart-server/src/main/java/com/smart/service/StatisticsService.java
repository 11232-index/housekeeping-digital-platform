package com.smart.service;

import com.smart.vo.OrderStatisticsVO;
import com.smart.vo.SalesRankVO;
import com.smart.vo.TurnoverStatisticsVO;
import com.smart.vo.UserStatisticsVO;

/**
 * 管理端工作台 - 统计服务
 */
public interface StatisticsService {

    /**
     * 营业额统计
     * @param beginTime 开始日期（yyyy-MM-dd，可为空）
     * @param endTime 结束日期（yyyy-MM-dd，可为空）
     * @return 总营业额、有效订单数、平均客单价及每日营业额
     */
    TurnoverStatisticsVO getTurnoverStatistics(String beginTime, String endTime);

    /**
     * 用户统计
     * @param beginTime 开始日期（yyyy-MM-dd，可为空）
     * @param endTime 结束日期（yyyy-MM-dd，可为空）
     * @return 用户总数、新增用户数及每日新增趋势
     */
    UserStatisticsVO getUserStatistics(String beginTime, String endTime);

    /**
     * 订单统计
     * @param beginTime 开始日期（yyyy-MM-dd，可为空）
     * @param endTime 结束日期（yyyy-MM-dd，可为空）
     * @return 订单总数、有效订单数、已取消订单数、每日趋势及状态分布
     */
    OrderStatisticsVO getOrderStatistics(String beginTime, String endTime);

    /**
     * 销量排名统计（Top 10）
     * @param beginTime 开始日期（yyyy-MM-dd，可为空）
     * @param endTime 结束日期（yyyy-MM-dd，可为空）
     * @return 服务名称、销量、销售额三个一一对应的列表
     */
    SalesRankVO getSalesRank(String beginTime, String endTime);

}
