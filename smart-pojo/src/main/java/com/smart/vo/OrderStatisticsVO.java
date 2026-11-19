package com.smart.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 订单统计（按下单时间聚合）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 统计区间内订单总数 */
    private Long totalOrder;

    /** 有效订单数（已完成且已支付） */
    private Long validOrder;

    /** 已取消订单数（含退款中与已取消） */
    private Long cancelledOrder;

    /** 日期列表，格式 yyyy-MM-dd（区间内每一天都有，无数据补 0） */
    private List<String> dateList;

    /** 每日订单数，与 dateList 一一对应 */
    private List<Long> orderList;

    /** 各状态订单数分布（只包含有订单的状态） */
    private List<StatusCountVO> statusList;

}
