package com.smart.controller.user;

import com.smart.entity.HousekeeperSchedule;
import com.smart.result.Result;
import com.smart.service.HousekeeperScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端 - 家政员可预约时段
 */
@RestController
@RequestMapping("/user/housekeeper-schedule")
@Slf4j
public class HousekeeperScheduleUserController {

    @Autowired
    private HousekeeperScheduleService scheduleService;

    /** 查询某家政员所有可预约时段（status=1） */
    @GetMapping("/available")
    public Result<List<HousekeeperSchedule>> available(@RequestParam Long housekeeperId) {
        log.info("C端查询可预约时段：housekeeperId={}", housekeeperId);
        List<HousekeeperSchedule> list = scheduleService.listAvailableByHousekeeperId(housekeeperId);
        return Result.success(list);
    }
}
