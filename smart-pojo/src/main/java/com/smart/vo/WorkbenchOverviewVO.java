package com.smart.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 员工端工作台 - 聚合数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkbenchOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 统计日期 yyyy-MM-dd */
    private String date;

    /** 待办事项 */
    private WorkbenchTodoVO todo;

    /** 当日经营指标 */
    private WorkbenchKpiVO kpi;

    /** 当日预约时段分布 */
    private List<ScheduleSlotVO> timeSlots;

}
