package com.smart.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单状态分布
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusCountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单状态值：1待付款 2待接单 3已接单 4服务中 5已完成 6退款中 7已取消 */
    private Integer status;

    /** 订单状态名称 */
    private String name;

    /** 该状态下的订单数 */
    private Long count;

}
