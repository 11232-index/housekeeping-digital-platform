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

    /** 家政员姓名（冗余，供员工端展示「已指派（张三）」而不是一串 id） */
    private String housekeeperName;
}
