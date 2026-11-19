package com.smart.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用户统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户总数（全表，不受统计区间影响） */
    private Long totalUser;

    /** 统计区间内新增用户数 */
    private Long newUser;

    /** 日期列表，格式 yyyy-MM-dd（区间内每一天都有，无数据补 0） */
    private List<String> dateList;

    /** 每日新增用户数，与 dateList 一一对应 */
    private List<Long> newUserList;

}
