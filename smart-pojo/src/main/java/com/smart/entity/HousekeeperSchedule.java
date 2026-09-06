package com.smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 家政员预约时间表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HousekeeperSchedule implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 关联家政员表id */
    private Long housekeeperId;

    /** 家政员姓名（关联查询，非表字段） */
    private String housekeeperName;

    /** 预约日期 */
    private LocalDate scheduleDate;

    /** 开始时间 */
    private LocalTime startTime;

    /** 结束时间 */
    private LocalTime endTime;

    /** 1可约 0不可约 */
    private Integer status;

    /** 创建者 */
    private Long createUser;

    /** 更新者 */
    private Long updateUser;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
