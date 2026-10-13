package com.smart.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * 预约时段解析工具
 * <p>
 * orders.schedule_time 是自由文本，且库里【两种格式混存】：
 * <ul>
 *   <li>用户端下单页给的是 {@code '8:00~9:00'} —— <b>小时不补零</b></li>
 *   <li>家政员预约模式下给的是 {@code '09:00~11:00'} —— WorkerProfileVO 已格式化成 HH:mm</li>
 * </ul>
 * 直接用 {@code LocalTime.parse("8:00")} 会抛异常（ISO_LOCAL_TIME 要求小时两位），
 * 所以这里按格式列表逐个尝试，并容忍全角波浪号和连字符分隔。
 * <p>
 * 调用方注意：解析失败必须当成业务错误拒绝掉，不能让 start/end 以 null 进入 SQL ——
 * 那样 {@code s.start_time < NULL} 恒为 false，会让「所有家政员都变成候选」。
 */
public class ScheduleTimeUtil {

    /** 支持的单个时间点格式（按尝试顺序） */
    private static final List<DateTimeFormatter> TIME_FORMATS = Arrays.asList(
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm:ss"),
            DateTimeFormatter.ofPattern("HH:mm:ss")
    );

    /** 时段分隔符：半角波浪号 / 全角波浪号 / 连字符 */
    private static final String SPLIT_REGEX = "\\s*[~\uFF5E\\-]\\s*";

    private ScheduleTimeUtil() {
    }

    /**
     * 解析单个时间点
     *
     * @param text 如 "8:00"、"09:00"、"9:00:00"
     * @return 解析结果；无法解析返回 null
     */
    public static LocalTime parseTime(String text) {
        if (text == null) {
            return null;
        }
        String s = text.trim();
        if (s.isEmpty()) {
            return null;
        }
        for (DateTimeFormatter fmt : TIME_FORMATS) {
            try {
                return LocalTime.parse(s, fmt);
            } catch (Exception ignored) {
                // 换下一种格式继续试
            }
        }
        return null;
    }

    /**
     * 解析时段文本
     *
     * @param timeRange 如 "09:00~11:00"、"8:00~9:00"
     * @return [开始, 结束]；任一端解析失败返回 null
     */
    public static LocalTime[] parseRange(String timeRange) {
        if (timeRange == null || timeRange.trim().isEmpty()) {
            return null;
        }
        String[] parts = timeRange.trim().split(SPLIT_REGEX);
        if (parts.length < 2) {
            return null;
        }
        LocalTime start = parseTime(parts[0]);
        LocalTime end = parseTime(parts[1]);
        if (start == null || end == null) {
            return null;
        }
        return new LocalTime[]{start, end};
    }

    /**
     * 解析「服务开始时刻」= 预约日期 + 时段的前半段。
     * 用于超时判断（距服务开始不足 N 小时仍未接单 → 转待退款）。
     *
     * @param date      预约日期
     * @param timeRange 时段文本
     * @return 服务开始时刻；任一为空或解析失败返回 null
     */
    public static LocalDateTime parseStart(LocalDate date, String timeRange) {
        if (date == null) {
            return null;
        }
        LocalTime[] range = parseRange(timeRange);
        if (range == null) {
            return null;
        }
        return LocalDateTime.of(date, range[0]);
    }
}
