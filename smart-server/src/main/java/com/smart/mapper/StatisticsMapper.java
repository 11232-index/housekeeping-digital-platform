package com.smart.mapper;

import com.smart.vo.DailyStatVO;
import com.smart.vo.SalesRankItemVO;
import com.smart.vo.StatusCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端工作台 - 统计相关数据访问层（只读聚合查询）
 */
@Mapper
public interface StatisticsMapper {

    // ==================== 营业额统计 ====================

    /**
     * 按付款日期统计每日营业额与有效订单数
     * @param beginTime 开始日期（yyyy-MM-dd）
     * @param endTime 结束日期（yyyy-MM-dd）
     * @return 每日统计列表
     */
    List<DailyStatVO> sumTurnoverByDay(@Param("beginTime") String beginTime, @Param("endTime") String endTime);

    /**
     * 统计区间内总营业额
     */
    BigDecimal sumTurnover(@Param("beginTime") String beginTime, @Param("endTime") String endTime);

    /**
     * 统计区间内有效订单数（已完成且已支付）
     */
    Long countValidOrders(@Param("beginTime") String beginTime, @Param("endTime") String endTime);

    // ==================== 用户统计 ====================

    /**
     * 用户总数
     */
    Long countTotalUsers();

    /**
     * 按注册日期统计每日新增用户数
     */
    List<DailyStatVO> countNewUsersByDay(@Param("beginTime") String beginTime, @Param("endTime") String endTime);

    // ==================== 订单统计 ====================

    /**
     * 统计区间内订单总数（按下单时间）
     */
    Long countOrders(@Param("beginTime") String beginTime, @Param("endTime") String endTime);

    /**
     * 按下单日期统计每日订单数
     */
    List<DailyStatVO> countOrdersByDay(@Param("beginTime") String beginTime, @Param("endTime") String endTime);

    /**
     * 统计区间内各状态订单数
     */
    List<StatusCountVO> countOrdersByStatus(@Param("beginTime") String beginTime, @Param("endTime") String endTime);

    // ==================== 销量排名统计 ====================

    /**
     * 统计区间内服务项目/服务套餐销量 Top 10（已完成且已支付订单的明细）
     */
    List<SalesRankItemVO> listTopSales(@Param("beginTime") String beginTime, @Param("endTime") String endTime);

}
