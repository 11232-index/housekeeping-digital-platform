package com.smart.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 销量排名统计（服务项目/服务套餐 Top 10，按已完成订单的明细单量排序）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesRankVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 服务名称列表（按销量从高到低） */
    private List<String> nameList;

    /** 销量列表（订单明细条数），与 nameList 一一对应 */
    private List<Long> numberList;

    /** 销售额列表（订单明细金额之和），与 nameList 一一对应 */
    private List<BigDecimal> amountList;

}
