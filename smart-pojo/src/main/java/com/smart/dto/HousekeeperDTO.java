package com.smart.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 家政员数据传输对象
 */
@Data
public class HousekeeperDTO implements Serializable {

    private Long id;

    /** 关联员工表id */
    private Long employeeId;

    /** 家政员名字 */
    private String name;

    /** 图片路径 */
    private String picture;

    /** 单价 */
    private BigDecimal price;

    /** 评分 */
    private BigDecimal rating;

    /** 身价 0无牌 1金牌 2银牌 3铜牌 */
    private Integer rank;

    /** 轮播图（多图URL，分号分隔） */
    private String carousel;

    /** 综合评价 */
    private String review;

}
