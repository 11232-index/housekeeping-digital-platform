package com.smart.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 优惠券（代金券）实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Voucher implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 优惠券类型：0普通券 */
    public static final Integer TYPE_NORMAL = 0;

    /** 优惠券类型：1秒杀券 */
    public static final Integer TYPE_SECKILL = 1;

    /** 优惠券状态：1上架 */
    public static final Integer STATUS_ON_SALE = 1;

    /** 优惠券状态：2下架 */
    public static final Integer STATUS_OFF_SALE = 2;

    //主键（RedisIdWorker全局ID生成器生成，序列化为字符串避免前端精度丢失）
    @JsonSerialize(using = ToStringSerializer.class)
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

    //创建人（配合 @AutoFill 自动填充使用，tb_voucher 表无此字段不落库）
    private Long createUser;

    //修改人（配合 @AutoFill 自动填充使用，tb_voucher 表无此字段不落库）
    private Long updateUser;

    //创建时间
    private LocalDateTime createTime;

    //更新时间
    private LocalDateTime updateTime;

}
