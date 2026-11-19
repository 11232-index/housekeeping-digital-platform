package com.smart.mapper;

import com.smart.vo.ScheduleSlotVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 员工端工作台数据访问层（只读聚合查询）
 */
@Mapper
public interface WorkbenchMapper {

    // ==================== 待办 ====================

    /**
     * 按订单状态统计数量
     */
    Long countOrdersByStatus(@Param("status") Integer status);

    /**
     * 待指派订单数（待接单且尚未指派家政员）
     */
    Long countPendingAssign();

    // ==================== 当日指标 ====================

    /**
     * 指定日期下单数
     * @param date yyyy-MM-dd
     */
    Long countOrdersByDate(@Param("date") String date);

    /**
     * 指定日期营业额（已完成且已支付，按付款日期）
     */
    BigDecimal sumTurnoverByDate(@Param("date") String date);

    /**
     * 指定日期有效订单数（已完成且已支付，按付款日期）
     */
    Long countValidOrdersByDate(@Param("date") String date);

    /**
     * 指定日期新增用户数
     */
    Long countNewUsersByDate(@Param("date") String date);

    /**
     * 指定日期的预约时段分布（按预约日期，排除待付款/退款/已取消）
     */
    List<ScheduleSlotVO> countAppointmentsBySlot(@Param("date") String date);

}
