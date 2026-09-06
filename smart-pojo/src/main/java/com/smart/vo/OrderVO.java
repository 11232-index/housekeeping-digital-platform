package com.smart.vo;

import com.smart.entity.Order;
import com.smart.entity.OrderDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 订单详情视图对象（继承Order，追加明细列表）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderVO extends Order {

    /** 订单明细列表 */
    private List<OrderDetail> orderDetailList;
}
