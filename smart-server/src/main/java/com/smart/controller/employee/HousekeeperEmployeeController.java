package com.smart.controller.employee;

import com.smart.annotation.RoleCheck;
import com.smart.dto.HousekeeperPageQueryDTO;
import com.smart.result.PageResult;
import com.smart.result.Result;
import com.smart.service.HousekeeperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 员工端 - 家政员查询
 */
@RestController
@RequestMapping("/employee/housekeeper")
@Slf4j
public class HousekeeperEmployeeController {

    @Autowired
    private HousekeeperService housekeeperService;

    /** 分页查询家政员列表（供下拉选择用） */
    @GetMapping("/page")
    @RoleCheck({"admin", "manager"})
    public Result<PageResult> page(HousekeeperPageQueryDTO dto) {
        log.info("员工端查询家政员列表：{}", dto);
        return Result.success(housekeeperService.pageQuery(dto));
    }

}
