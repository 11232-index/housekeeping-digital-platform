package com.smart.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 员工端指派时的候选家政员VO
 * <p>
 * 只包含「该订单服务时段内空闲」的家政员（判定依据见 HousekeeperMapper#listCandidates）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HousekeeperCandidateVO implements Serializable {

    /** 家政员id */
    private Long housekeeperId;

    /** 家政员姓名 */
    private String name;

    /** 头像 */
    private String picture;

    /** 单价 */
    private BigDecimal price;

    /** 评分 */
    private BigDecimal rating;

    /** 身价 0无牌 1金牌 2银牌 3铜牌 */
    private Integer rank;
}
