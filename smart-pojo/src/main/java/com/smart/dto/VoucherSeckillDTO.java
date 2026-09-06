package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 优惠券领取/秒杀DTO
 */
@Data
public class VoucherSeckillDTO implements Serializable {

    //要领取/秒杀的优惠券id
    private Long voucherId;

}
