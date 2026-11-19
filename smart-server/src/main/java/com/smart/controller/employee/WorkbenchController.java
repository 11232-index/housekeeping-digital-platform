package com.smart.controller.employee;

import com.smart.result.Result;
import com.smart.service.WorkbenchService;
import com.smart.vo.WorkbenchOverviewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 员工端 - 工作台
 */
@RestController
@RequestMapping("/employee/workbench")
@Slf4j
public class WorkbenchController {

    @Autowired
    private WorkbenchService workbenchService;

    /**
     * 工作台聚合数据（待办 + 当日指标 + 当日预约时段）
     * @param date 统计日期（yyyy-MM-dd，不传默认今天）
     */
    @GetMapping("/overview")
    public Result<WorkbenchOverviewVO> overview(@RequestParam(value = "date", required = false) String date) {
        log.info("员工端工作台查询：date={}", date);
        return Result.success(workbenchService.getOverview(date));
    }

}
