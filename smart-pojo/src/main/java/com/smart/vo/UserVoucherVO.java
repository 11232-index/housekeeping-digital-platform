package com.smart.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户优惠券视图对象（「我的优惠券」列表用）
 * <p>
 * 把「领取记录(tb_user_voucher)」和「券本身(tb_voucher)」合在一起返回，
 * 前端一次就能渲染出券面信息 + 使用状态。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVoucherVO implements Serializable {

    /** 领取记录id */
    private Long id;

    /**
     * 优惠券id。
     * 券id是雪花id（19位），超出 JS 的安全整数范围（16位），
     * 必须序列化成字符串，否则前端解析时低位会被舍入、再传回来就查不到券了
     * （与 VoucherVO#id 的处理保持一致）。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long voucherId;

    /** 券标题 */
    private String title;

    /** 副标题 */
    private String subTitle;

    /** 使用规则 */
    private String rules;

    /** 折扣率（80=八折） */
    private Long actualValue;

    /** 券类型：0普通券 1秒杀券 */
    private Integer type;

    /** 使用状态：0未使用 1已使用 */
    private Integer status;

    /** 领取时间 */
    private LocalDateTime claimTime;

    /** 使用时间 */
    private LocalDateTime useTime;
}
