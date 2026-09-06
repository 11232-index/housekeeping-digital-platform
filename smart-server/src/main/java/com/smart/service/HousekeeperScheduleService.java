package com.smart.service;

import com.smart.dto.HousekeeperScheduleDTO;
import com.smart.dto.HousekeeperSchedulePageQueryDTO;
import com.smart.entity.HousekeeperSchedule;
import com.smart.result.PageResult;

import java.util.List;

public interface HousekeeperScheduleService {

    /** 新增预约时间 */
    void save(HousekeeperScheduleDTO dto);

    /** 分页查询 */
    PageResult pageQuery(HousekeeperSchedulePageQueryDTO dto);

    /** 根据id查询 */
    HousekeeperSchedule getById(Long id);

    /** 修改预约时间 */
    void update(HousekeeperScheduleDTO dto);

    /** 删除 */
    void delete(Long id);

    /** 批量删除 */
    void deleteBatch(List<Long> ids);

    /** 批量更新状态 */
    void updateStatusBatch(List<Long> ids, Integer status);

    /** 更新单条预约时间的状态 */
    void updateStatus(Long id, Integer status);

    /** 乐观抢位：仅当该时段当前可约(1)时占用为不可约(0)；true=抢到 */
    boolean tryOccupy(Long scheduleId);

    /** 乐观释放：仅当该时段当前被占(0)时放回可约(1)；true=释放成功 */
    boolean tryRelease(Long scheduleId);

    /** C端-查询某家政员所有可约时段 */
    List<HousekeeperSchedule> listAvailableByHousekeeperId(Long housekeeperId);
}
