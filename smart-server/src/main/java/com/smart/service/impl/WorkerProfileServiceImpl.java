package com.smart.service.impl;

import com.smart.entity.Employee;
import com.smart.entity.EmployeeDetail;
import com.smart.entity.Housekeeper;
import com.smart.entity.HousekeeperSchedule;
import com.smart.exception.BaseException;
import com.smart.mapper.EmployeeDetailMapper;
import com.smart.mapper.EmployeeMapper;
import com.smart.mapper.HousekeeperMapper;
import com.smart.service.HousekeeperScheduleService;
import com.smart.service.WorkerProfileService;
import com.smart.vo.WorkerProfileVO;
import com.smart.vo.WorkerScheduleSlotVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 家政员端"我的资料"实现
 */
@Slf4j
@Service
public class WorkerProfileServiceImpl implements WorkerProfileService {

    /** 可预约时段最多返回条数，避免数据量大时把整个列表塞给小程序 */
    private static final int MAX_SLOTS = 20;

    /** 时间格式化成 HH:mm（不要依赖默认序列化，LocalTime 会输出成 09:00:00） */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private EmployeeDetailMapper employeeDetailMapper;

    @Autowired
    private HousekeeperMapper housekeeperMapper;

    @Autowired
    private HousekeeperScheduleService housekeeperScheduleService;

    @Override
    public WorkerProfileVO getProfile(Long employeeId) {
        if (employeeId == null) {
            throw new BaseException("未登录，请先登录");
        }

        Employee employee = employeeMapper.getById(employeeId);
        if (employee == null) {
            throw new BaseException("员工不存在");
        }
        // 详情和家政员记录都可能不存在：员工不一定填过详细信息，也不一定有 housekeeper 记录
        EmployeeDetail detail = employeeDetailMapper.getByEmployeeId(employeeId);
        Housekeeper housekeeper = housekeeperMapper.getByEmployeeId(employeeId);

        log.info("家政员端查询我的资料：employeeId={}, housekeeperId={}, 有详情={}",
                employeeId, housekeeper == null ? null : housekeeper.getId(), detail != null);

        return WorkerProfileVO.builder()
                // 员工基本信息
                .employeeId(employee.getId())
                .name(employee.getName())
                .phone(employee.getPhone())
                .sex(employee.getSex())
                .age(employee.getAge())
                // 员工详细信息（可能整块为空）
                .education(detail != null ? detail.getEducation() : null)
                .pastExperience(detail != null ? detail.getPastExperience() : null)
                .position(detail != null ? detail.getPosition() : null)
                .salary(detail != null ? detail.getSalary() : null)
                .workStatus(detail != null ? detail.getWorkStatus() : null)
                .schedule(detail != null ? detail.getSchedule() : null)
                .contract(detail != null ? detail.getContract() : null)
                .healthCertificate(detail != null ? detail.getHealthCertificate() : null)
                // 家政员信息（可能整块为空）
                .housekeeperId(housekeeper != null ? housekeeper.getId() : null)
                .picture(housekeeper != null ? housekeeper.getPicture() : null)
                .price(housekeeper != null ? housekeeper.getPrice() : null)
                .rank(housekeeper != null ? housekeeper.getRank() : null)
                .rating(housekeeper != null ? housekeeper.getRating() : null)
                .review(housekeeper != null ? housekeeper.getReview() : null)
                .availableSlots(listAvailableSlots(housekeeper))
                .build();
    }

    /**
     * 查询该家政员今天及以后的可约时段
     * <p>
     * 复用 C 端已有方法（内部已按 status=1 过滤并按 schedule_date, start_time 排序），
     * 在这里做"日期 >= 今天"的过滤和格式化。用 stream 产生新 List，
     * 不直接改动缓存返回的集合（该方法带 @Cacheable）。
     */
    private List<WorkerScheduleSlotVO> listAvailableSlots(Housekeeper housekeeper) {
        if (housekeeper == null) {
            return Collections.emptyList();
        }

        List<HousekeeperSchedule> all = housekeeperScheduleService.listAvailableByHousekeeperId(housekeeper.getId());
        if (all == null || all.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate today = LocalDate.now();
        return all.stream()
                .filter(s -> s.getScheduleDate() != null && !s.getScheduleDate().isBefore(today))
                .filter(s -> s.getStartTime() != null && s.getEndTime() != null)
                .limit(MAX_SLOTS)
                .map(s -> WorkerScheduleSlotVO.builder()
                        .id(s.getId())
                        .scheduleDate(s.getScheduleDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .startTime(s.getStartTime().format(TIME_FORMATTER))
                        .endTime(s.getEndTime().format(TIME_FORMATTER))
                        .status(s.getStatus())
                        .build())
                .collect(Collectors.toList());
    }
}
