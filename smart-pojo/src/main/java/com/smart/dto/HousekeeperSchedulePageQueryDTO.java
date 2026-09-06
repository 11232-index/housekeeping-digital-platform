package com.smart.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 家政员预约时间分页查询DTO
 */
@Data
public class HousekeeperSchedulePageQueryDTO implements Serializable {

    /** 家政员id */
    private Long housekeeperId;

    /** 预约日期 */
    private String scheduleDate;

    /** 页码 */
    private int page;

    /** 每页记录数 */
    private int pageSize;

}
