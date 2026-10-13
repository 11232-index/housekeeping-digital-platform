package com.smart.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 家政员端可预约时段（单条）
 * <p>
 * 日期/时间刻意用 String 而不是 LocalDate/LocalTime：全局 ObjectMapper 只为 LocalDateTime
 * 注册了 yyyy-MM-dd HH:mm:ss 格式化器，LocalTime 会按 jsr310 默认输出成 "09:00:00"（带秒），
 * 小程序端会原样渲染。这里统一格式化成 "2026-09-20" / "09:00"。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerScheduleSlotVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 时段id */
    private Long id;

    /** 预约日期，格式 yyyy-MM-dd */
    private String scheduleDate;

    /** 开始时间，格式 HH:mm */
    private String startTime;

    /** 结束时间，格式 HH:mm */
    private String endTime;

    /** 1可约 0已占用 */
    private Integer status;

}
