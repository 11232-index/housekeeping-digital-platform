package com.smart.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 家政员预约时间DTO
 */
@Data
public class HousekeeperScheduleDTO implements Serializable {

    private Long id;

    /** 关联家政员表id */
    private Long housekeeperId;

    /** 预约日期 yyyy-MM-dd */
    private String scheduleDate;

    /** 开始时间 HH:mm */
    private String startTime;

    /** 结束时间 HH:mm */
    private String endTime;

    /** 0可约 1已约 */
    private Integer status;

}
