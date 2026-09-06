package com.smart.service;

import com.smart.dto.HousekeeperDTO;
import com.smart.dto.HousekeeperPageQueryDTO;
import com.smart.entity.Housekeeper;
import com.smart.result.PageResult;
import com.smart.vo.HousekeeperEmployeeVO;

public interface HousekeeperService {

    /** 新增家政员（员工role=cleaner时调用） */
    void save(HousekeeperDTO dto);

    /** 分页查询 */
    PageResult pageQuery(HousekeeperPageQueryDTO dto);

    /** 根据id查询 */
    Housekeeper getById(Long id);

    /** 修改家政员 */
    void update(HousekeeperDTO dto);

    /** 根据员工id删除家政员（员工role从cleaner改为其他时调用） */
    void deleteByEmployeeId(Long employeeId);

    /**
     * 根据家政员ID查询关联的员工信息（含员工详细信息）
     * 用于家政员详情页"下盒子"区域展示
     *
     * @param housekeeperId 家政员ID
     * @return 员工基础信息 + 详细信息 VO
     */
    HousekeeperEmployeeVO getEmployeeByHousekeeperId(Long housekeeperId);

}
