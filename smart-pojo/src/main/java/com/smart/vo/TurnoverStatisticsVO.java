package com.smart.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 营业额统计（已完成且已支付的订单，按下单付款日期聚合）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnoverStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 统计区间内总营业额 */
    private BigDecimal totalTurnover;

    /** 有效订单数（已完成且已支付） */
    private Long validOrderCount;

    /** 平均客单价 = 总营业额 / 有效订单数（无有效订单时为 0） */
    private BigDecimal avgOrderAmount;

    /** 日期列表，格式 yyyy-MM-dd（区间内每一天都有，无数据补 0） */
    private List<String> dateList;

    /** 每日营业额，与 dateList 一一对应 */
    private List<BigDecimal> turnoverList;

}
