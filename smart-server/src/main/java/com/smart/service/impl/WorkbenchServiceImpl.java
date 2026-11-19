package com.smart.service.impl;

import com.smart.context.BaseContext;
import com.smart.entity.Employee;
import com.smart.entity.Order;
import com.smart.exception.BaseException;
import com.smart.mapper.EmployeeMapper;
import com.smart.mapper.WorkbenchMapper;
import com.smart.service.WorkbenchService;
import com.smart.vo.ScheduleSlotVO;
import com.smart.vo.WorkbenchKpiVO;
import com.smart.vo.WorkbenchOverviewVO;
import com.smart.vo.WorkbenchTodoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 员工端工作台服务实现
 */
@Slf4j
@Service
public class WorkbenchServiceImpl implements WorkbenchService {

    @Autowired
    private WorkbenchMapper workbenchMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public WorkbenchOverviewVO getOverview(String date) {
        LocalDate day = parseDate(date);
        String dayStr = day.toString();

        // 待办：全部未处理订单，不限定日期（今天的待办可能来自明天/后天的预约）
        WorkbenchTodoVO todo = WorkbenchTodoVO.builder()
                .pendingAccept(nvl(workbenchMapper.countOrdersByStatus(Order.STATUS_PENDING_ACCEPT)))
                .pendingAssign(nvl(workbenchMapper.countPendingAssign()))
                .refunding(nvl(workbenchMapper.countOrdersByStatus(Order.STATUS_REFUNDING)))
                .build();

        // 当日预约时段
        List<ScheduleSlotVO> timeSlots = workbenchMapper.countAppointmentsBySlot(dayStr);
        long appointmentCount = timeSlots.stream()
                .mapToLong(item -> item.getCount() == null ? 0L : item.getCount())
                .sum();

        Long orderCount = nvl(workbenchMapper.countOrdersByDate(dayStr));
        Long newUser = nvl(workbenchMapper.countNewUsersByDate(dayStr));
        WorkbenchKpiVO.WorkbenchKpiVOBuilder kpiBuilder = WorkbenchKpiVO.builder()
                .orderCount(orderCount)
                .appointmentCount(appointmentCount)
                .newUser(newUser);

        // 钱相关数据只给 admin / manager；其他角色返回 null，前端也不渲染
        boolean canViewMoney = canViewMoney();
        if (canViewMoney) {
            BigDecimal turnover = workbenchMapper.sumTurnoverByDate(dayStr);
            if (turnover == null) {
                turnover = BigDecimal.ZERO;
            }
            Long validOrderCount = nvl(workbenchMapper.countValidOrdersByDate(dayStr));
            BigDecimal avgOrderAmount = validOrderCount == 0
                    ? BigDecimal.ZERO
                    : turnover.divide(BigDecimal.valueOf(validOrderCount), 2, RoundingMode.HALF_UP);
            kpiBuilder.turnover(turnover)
                    .validOrderCount(validOrderCount)
                    .avgOrderAmount(avgOrderAmount);
        }

        log.info("员工端工作台：date={}，待办={}，当日订单={}，预约={}，可看营业数据={}",
                dayStr, todo, orderCount, appointmentCount, canViewMoney);

        return WorkbenchOverviewVO.builder()
                .date(dayStr)
                .todo(todo)
                .kpi(kpiBuilder.build())
                .timeSlots(timeSlots)
                .build();
    }

    /**
     * 当前登录员工是否可见营业额 / 客单价
     */
    private boolean canViewMoney() {
        Long employeeId = BaseContext.getCurrentId();
        if (employeeId == null) {
            return false;
        }
        Employee employee = employeeMapper.getById(employeeId);
        if (employee == null) {
            return false;
        }
        return "admin".equals(employee.getRole()) || "manager".equals(employee.getRole());
    }

    /**
     * 解析日期，不传默认今天
     */
    private LocalDate parseDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(date.trim());
        } catch (DateTimeParseException e) {
            throw new BaseException("日期格式错误，应为 yyyy-MM-dd");
        }
    }

    private Long nvl(Long value) {
        return value == null ? 0L : value;
    }

}
