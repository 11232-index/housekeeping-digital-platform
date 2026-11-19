package com.smart.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销量排名单行数据（Mapper 查询结果载体）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesRankItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 服务名称（下单时的名称快照） */
    private String name;

    /** 销量（订单明细条数） */
    private Long number;

    /** 销售额（订单明细金额之和） */
    private BigDecimal amount;

}
