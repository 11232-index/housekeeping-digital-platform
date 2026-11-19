package com.smart.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 按天聚合的统计结果（Mapper 查询结果载体）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日期，格式 yyyy-MM-dd */
    private String date;

    /** 数量（订单数/用户数） */
    private Long count;

    /** 金额（仅营业额统计使用） */
    private BigDecimal amount;

}
