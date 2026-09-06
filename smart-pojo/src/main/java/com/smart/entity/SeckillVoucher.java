package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 秒杀优惠券实体（与 tb_voucher 一对一）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillVoucher implements Serializable {

    private static final long serialVersionUID = 1L;

    //关联的优惠券id
    private Long voucherId;

    //库存
    private Integer stock;

    //生效时间
    private LocalDateTime beginTime;

    //失效时间
    private LocalDateTime endTime;

    //创建时间
    private LocalDateTime createTime;

    //更新时间
    private LocalDateTime updateTime;

}
