package com.smart.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 员工端工作台 - 当日经营指标
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkbenchKpiVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当日下单数（按 order_time） */
    private Long orderCount;

    /** 当日预约数（按 schedule_date，排除待付款/退款/已取消） */
    private Long appointmentCount;

    /** 当日新增用户数 */
    private Long newUser;

    /** 当日营业额（仅 admin/manager 可见，其余角色为 null） */
    private BigDecimal turnover;

    /** 当日有效订单数（仅 admin/manager 可见，其余角色为 null） */
    private Long validOrderCount;

    /** 平均客单价（仅 admin/manager 可见，其余角色为 null） */
    private BigDecimal avgOrderAmount;

}
