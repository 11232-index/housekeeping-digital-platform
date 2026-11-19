package com.smart.controller.admin;

import com.smart.result.Result;
import com.smart.service.StatisticsService;
import com.smart.vo.OrderStatisticsVO;
import com.smart.vo.SalesRankVO;
import com.smart.vo.TurnoverStatisticsVO;
import com.smart.vo.UserStatisticsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 - 工作台统计
 */
@RestController
@RequestMapping("/admin/statistics")
@Slf4j
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 营业额统计
     * @param beginTime 开始日期（yyyy-MM-dd，不传默认近 30 天）
     * @param endTime 结束日期（yyyy-MM-dd，不传默认近 30 天）
     */
    @GetMapping("/turnover")
    public Result<TurnoverStatisticsVO> turnover(@RequestParam(value = "beginTime", required = false) String beginTime,
                                                 @RequestParam(value = "endTime", required = false) String endTime) {
        log.info("营业额统计：{} ~ {}", beginTime, endTime);
        return Result.success(statisticsService.getTurnoverStatistics(beginTime, endTime));
    }

    /**
     * 用户统计
     * @param beginTime 开始日期（yyyy-MM-dd，不传默认近 30 天）
     * @param endTime 结束日期（yyyy-MM-dd，不传默认近 30 天）
     */
    @GetMapping("/user")
    public Result<UserStatisticsVO> user(@RequestParam(value = "beginTime", required = false) String beginTime,
                                         @RequestParam(value = "endTime", required = false) String endTime) {
        log.info("用户统计：{} ~ {}", beginTime, endTime);
        return Result.success(statisticsService.getUserStatistics(beginTime, endTime));
    }

    /**
     * 订单统计
     * @param beginTime 开始日期（yyyy-MM-dd，不传默认近 30 天）
     * @param endTime 结束日期（yyyy-MM-dd，不传默认近 30 天）
     */
    @GetMapping("/order")
    public Result<OrderStatisticsVO> order(@RequestParam(value = "beginTime", required = false) String beginTime,
                                           @RequestParam(value = "endTime", required = false) String endTime) {
        log.info("订单统计：{} ~ {}", beginTime, endTime);
        return Result.success(statisticsService.getOrderStatistics(beginTime, endTime));
    }

    /**
     * 销量排名统计（Top 10）
     * @param beginTime 开始日期（yyyy-MM-dd，不传默认近 30 天）
     * @param endTime 结束日期（yyyy-MM-dd，不传默认近 30 天）
     */
    @GetMapping("/sales")
    public Result<SalesRankVO> sales(@RequestParam(value = "beginTime", required = false) String beginTime,
                                     @RequestParam(value = "endTime", required = false) String endTime) {
        log.info("销量排名统计：{} ~ {}", beginTime, endTime);
        return Result.success(statisticsService.getSalesRank(beginTime, endTime));
    }

}
