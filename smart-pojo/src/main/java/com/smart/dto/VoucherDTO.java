package com.smart.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 优惠券数据传输对象
 */
@Data
public class VoucherDTO implements Serializable {

    private Long id;

    //使用该优惠券的订单id（关联 orders 表），支付时使用券后回填
    private Long orderId;

    //代金券标题
    private String title;

    //副标题
    private String subTitle;

    //使用规则
    private String rules;

    //原价，单位是分（展示用）
    private Long payValue;

    //折扣率（八折=80，八五折=85），实付=订单总价×actualValue÷100
    private Long actualValue;

    //类型：0普通券；1秒杀券
    private Integer type;

    //状态：1上架；2下架；3过期
    private Integer status;

    //秒杀券专属：库存
    private Integer stock;

    //秒杀券专属：生效时间
    private LocalDateTime beginTime;

    //秒杀券专属：失效时间
    private LocalDateTime endTime;

}
