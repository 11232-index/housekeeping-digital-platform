package com.smart.controller.user;

import com.smart.dto.HousekeeperPageQueryDTO;
import com.smart.entity.Housekeeper;
import com.smart.result.PageResult;
import com.smart.result.Result;
import com.smart.service.HousekeeperService;
import com.smart.vo.HousekeeperEmployeeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端 - 家政员
 */
@RestController
@RequestMapping("/user/housekeeper")
@Slf4j
public class HousekeeperUserController {

    @Autowired
    private HousekeeperService housekeeperService;

    /** 查询前10条家政员（供首页推荐） */
    @GetMapping("/top10")
    public Result<List<Housekeeper>> top10() {
        HousekeeperPageQueryDTO dto = new HousekeeperPageQueryDTO();
        dto.setPage(1);
        dto.setPageSize(10);
        PageResult result = housekeeperService.pageQuery(dto);
        @SuppressWarnings("unchecked")
        List<Housekeeper> list = result.getRecords();
        log.info("C端查询家政员推荐，共{}条", list.size());
        return Result.success(list);
    }

    /** 分页查询家政员（支持名字搜索） */
    @GetMapping("/page")
    public Result<PageResult> page(HousekeeperPageQueryDTO dto) {
        log.info("C端家政员分页查询：{}", dto);
        PageResult result = housekeeperService.pageQuery(dto);
        return Result.success(result);
    }

    /** 根据id查询家政员详情 */
    @GetMapping("/{id}")
    public Result<Housekeeper> getById(@PathVariable Long id) {
        log.info("C端查询家政员详情：id={}", id);
        return Result.success(housekeeperService.getById(id));
    }

    /**
     * 根据家政员ID查询关联的员工信息（含员工详细信息）
     * 用于家政员详情页"下盒子"区域展示
     *
     * 数据来源：
     * - 家政员表 → 获取 employeeId
     * - 员工表（Employee）→ 基础信息（姓名、手机、性别、年龄）
     * - 员工详情表（EmployeeDetail）→ 详细信息（学历、经验、职位、在职状态、健康证）
     *
     * 缓存策略：优先从 Redis 读取，未命中再查 MySQL
     */
    @GetMapping("/{id}/employee")
    public Result<HousekeeperEmployeeVO> getEmployeeByHousekeeperId(@PathVariable Long id) {
        log.info("C端查询家政员关联员工信息：housekeeperId={}", id);
        HousekeeperEmployeeVO vo = housekeeperService.getEmployeeByHousekeeperId(id);
        return Result.success(vo);
    }

}
