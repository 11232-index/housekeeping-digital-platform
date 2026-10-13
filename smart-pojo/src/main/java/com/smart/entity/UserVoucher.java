package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户优惠券实体（领取与使用记录）
 * <p>
 * 对应 tb_user_voucher 表：一条记录 = 某个用户领了某张券。
 * 原来"谁领了哪张券"只记在 Redis 的 Set 里，这条表是可靠来源（唯一索引兜底一人一单）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVoucher implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态：0未使用 */
    public static final Integer STATUS_UNUSED = 0;
    /** 状态：1已使用 */
    public static final Integer STATUS_USED = 1;

    /** 主键 */
    private Long id;

    /** 用户id */
    private Long userId;

    /** 优惠券id（关联 tb_voucher） */
    private Long voucherId;

    /** 状态：0未使用 1已使用 */
    private Integer status;

    /** 使用该券的订单id（用掉后回填） */
    private Long orderId;

    /** 领取时间 */
    private LocalDateTime claimTime;

    /** 使用时间 */
    private LocalDateTime useTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
