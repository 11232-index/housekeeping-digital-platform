package com.smart.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 员工端工作台 - 当日预约时段分布
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSlotVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 预约时段，如 09:00~11:00 */
    private String time;

    /** 该时段的预约单数 */
    private Long count;

}
