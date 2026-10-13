package com.smart.vo;

import com.smart.entity.OrderDetail;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 家政员端订单视图对象
 * <p>
 * 只带家政员需要的信息（服务内容、时间、地址、联系人等），
 * 刻意不含 payStatus / payMethod 这类支付状态细节。
 */
@Data
public class WorkerOrderVO implements Serializable {

    /** 订单id */
    private Long id;

    /** 订单号 */
    private String number;

    /** 订单状态：2待接单 3已接单 4服务中 5已完成 6待退款 7已取消 */
    private Integer status;

    /** 订单金额 */
    private java.math.BigDecimal amount;

    /** 预约日期 */
    private LocalDate scheduleDate;

    /** 预约时段（如：09:00~11:00） */
    private String scheduleTime;

    /** 服务时长 */
    private String serviceDuration;

    /** 联系人 */
    private String contactName;

    /** 手机号 */
    private String phone;

    /** 详细地址 */
    private String address;

    /** 备注 */
    private String remark;

    /** 预计上门时间 */
    private LocalDateTime estimatedServiceTime;

    /** 实际上门时间 */
    private LocalDateTime actualServiceTime;

    /** 取消原因（含「不接单原因」，即订单为何转入待退款） */
    private String cancelReason;

    /** 订单明细列表 */
    private List<OrderDetail> orderDetailList;
}
