package com.smart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 优惠券分页查询DTO
 */
@Data
public class VoucherPageQueryDTO implements Serializable {

    //标题（模糊搜索）
    private String title;

    //类型：0普通券；1秒杀券
    private Integer type;

    //状态：1上架；2下架；3过期
    private Integer status;

    //页码
    private int page;

    //每页显示记录数
    private int pageSize;

}
