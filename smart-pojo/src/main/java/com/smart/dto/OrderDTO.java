package com.smart.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单通用更新传输对象
 */
@Data
public class OrderDTO implements Serializable {

    /** 订单id */
    private Long id;

    /** 地址id */
    private Long addressBookId;

    /** 备注 */
    private String remark;

    /** 手机号 */
    private String phone;

    /** 详细地址 */
    private String address;

    /** 联系人 */
    private String contactName;

    /** 预计上门时间 */
    private LocalDateTime estimatedServiceTime;

    /** 订单状态 */
    private Integer status;

    /** 取消原因 */
    private String cancelReason;
}
