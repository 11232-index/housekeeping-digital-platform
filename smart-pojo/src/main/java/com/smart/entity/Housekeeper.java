package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 家政员
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Housekeeper implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 关联员工表id */
    private Long employeeId;

    /** 家政员名字（关联员工name） */
    private String name;

    /** 图片路径 */
    private String picture;

    /** 单价 */
    private BigDecimal price;

    /** 评分 */
    private BigDecimal rating;

    /** 身价 0无牌 1金牌 2银牌 3铜牌 */
    private Integer rank;

    /** 轮播图（多图URL，分号分隔，如 url1;url2;url3） */
    private String carousel;

    /** 综合评价 */
    private String review;

    /** 创建人 */
    private Long createUser;

    /** 修改人 */
    private Long updateUser;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
