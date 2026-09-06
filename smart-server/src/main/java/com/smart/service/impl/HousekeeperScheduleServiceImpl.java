package com.smart.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.smart.dto.HousekeeperScheduleDTO;
import com.smart.dto.HousekeeperSchedulePageQueryDTO;
import com.smart.entity.HousekeeperSchedule;
import com.smart.exception.BaseException;
import com.smart.mapper.HousekeeperScheduleMapper;
import com.smart.result.PageResult;
import com.smart.service.HousekeeperScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class HousekeeperScheduleServiceImpl implements HousekeeperScheduleService {

    @Autowired
    private HousekeeperScheduleMapper mapper;

    /** 新增预约时间 */
    @Override
    @CacheEvict(value = "housekeeperSchedule", allEntries = true)
    public void save(HousekeeperScheduleDTO dto) {
        // 防重复：检查同一家政员同一日期同一时段是否已存在
        LocalDate scheduleDate = LocalDate.parse(dto.getScheduleDate(), DateTimeFormatter.ISO_LOCAL_DATE);
        LocalTime startTime = LocalTime.parse(dto.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime endTime = LocalTime.parse(dto.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));
        HousekeeperSchedule existing = mapper.getByHousekeeperAndTime(dto.getHousekeeperId(), scheduleDate, startTime, endTime);
        if (existing != null) {
            throw new BaseException("该时段已存在，请勿重复添加");
        }
        HousekeeperSchedule schedule = new HousekeeperSchedule();
        schedule.setHousekeeperId(dto.getHousekeeperId());
        schedule.setScheduleDate(scheduleDate);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        mapper.insert(schedule);
        log.info("新增家政员预约时间，housekeeperId={}, date={}, {}-{}",
                dto.getHousekeeperId(), dto.getScheduleDate(), dto.getStartTime(), dto.getEndTime());
    }

    /** 分页查询 */
    @Override
    @Cacheable(value = "housekeeperSchedule", key = "'page:' + #dto.housekeeperId + ':' + #dto.scheduleDate + ':' + #dto.page + ':' + #dto.pageSize", sync = true)
    public PageResult pageQuery(HousekeeperSchedulePageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<HousekeeperSchedule> page = mapper.pageQuery(dto);
        log.debug("家政员预约时间分页查询，housekeeperId={}, 共{}条", dto.getHousekeeperId(), page.getTotal());
        return new PageResult(page.getTotal(), page.getResult());
    }

    /** 根据id查询 */
    @Override
    @Cacheable(value = "housekeeperSchedule", key = "'one:' + #id", sync = true)
    public HousekeeperSchedule getById(Long id) {
        return mapper.getById(id);
    }

    /** 修改预约时间 */
    @Override
    @CacheEvict(value = "housekeeperSchedule", allEntries = true)
    public void update(HousekeeperScheduleDTO dto) {
        if (dto.getId() == null) {
            throw new BaseException("预约时段id不能为空");
        }
        HousekeeperSchedule current = mapper.getById(dto.getId());
        if (current == null) {
            throw new BaseException("该预约时段不存在");
        }
        // 目标值：没传的字段沿用原值，避免只改单个字段时漏校验
        LocalDate scheduleDate = dto.getScheduleDate() != null
                ? LocalDate.parse(dto.getScheduleDate(), DateTimeFormatter.ISO_LOCAL_DATE)
                : current.getScheduleDate();
        LocalTime startTime = dto.getStartTime() != null
                ? LocalTime.parse(dto.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"))
                : current.getStartTime();
        LocalTime endTime = dto.getEndTime() != null
                ? LocalTime.parse(dto.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"))
                : current.getEndTime();
        Long housekeeperId = dto.getHousekeeperId() != null ? dto.getHousekeeperId() : current.getHousekeeperId();
        if (scheduleDate == null || startTime == null || endTime == null || housekeeperId == null) {
            throw new BaseException("家政员、日期、开始/结束时间不完整");
        }
        if (!startTime.isBefore(endTime)) {
            throw new BaseException("结束时间必须晚于开始时间");
        }
        // 修改成“完全相同”去重：与当天其他完全相同（起止一致）的时段互斥，排除自身；时间重叠不在本次处理范围
        HousekeeperSchedule dup = mapper.getByHousekeeperAndTime(housekeeperId, scheduleDate, startTime, endTime);
        if (dup != null && !dup.getId().equals(dto.getId())) {
            throw new BaseException("修改后的时段与该家政员当天已有时段完全相同，请勿重复");
        }
        HousekeeperSchedule schedule = new HousekeeperSchedule();
        schedule.setId(dto.getId());
        schedule.setHousekeeperId(housekeeperId);
        schedule.setScheduleDate(scheduleDate);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        if (dto.getStatus() != null) {
            schedule.setStatus(dto.getStatus());
        }
        mapper.update(schedule);
        log.info("修改家政员预约时间，id={}", dto.getId());
    }

    /** 删除 */
    @Override
    @CacheEvict(value = "housekeeperSchedule", allEntries = true)
    public void delete(Long id) {
        mapper.delete(id);
        log.info("删除家政员预约时间，id={}", id);
    }

    /** 批量删除 */
    @Override
    @CacheEvict(value = "housekeeperSchedule", allEntries = true)
    public void deleteBatch(List<Long> ids) {
        mapper.deleteBatch(ids);
        log.info("批量删除家政员预约时间，共{}条", ids.size());
    }

    /** 批量更新状态 */
    @Override
    @CacheEvict(value = "housekeeperSchedule", allEntries = true)
    public void updateStatusBatch(List<Long> ids, Integer status) {
        mapper.updateStatusBatch(ids, status);
        log.info("批量更新家政员预约时间状态，共{}条，status={}", ids.size(), status);
    }

    /** 更新单条预约时间的状态 */
    @Override
    @CacheEvict(value = "housekeeperSchedule", allEntries = true)
    public void updateStatus(Long id, Integer status) {
        HousekeeperSchedule schedule = new HousekeeperSchedule();
        schedule.setId(id);
        schedule.setStatus(status);
        mapper.update(schedule);
        log.info("更新预约时间状态：id={}, status={}", id, status);
    }

    /** 乐观抢位：仅当当前为可约(1)时置为已占用(0)，并发下由数据库条件更新保证只有一人成功 */
    @Override
    @CacheEvict(value = "housekeeperSchedule", allEntries = true)
    public boolean tryOccupy(Long scheduleId) {
        boolean ok = mapper.occupy(scheduleId) == 1;
        log.info("家政员预约时段抢位：scheduleId={}, {}", scheduleId, ok ? "成功" : "已被预约");
        return ok;
    }

    /** 乐观释放：仅当当前为已占用(0)时放回可约(1)，重复/迟到取消不会误释放他人占用 */
    @Override
    @CacheEvict(value = "housekeeperSchedule", allEntries = true)
    public boolean tryRelease(Long scheduleId) {
        boolean ok = mapper.release(scheduleId) == 1;
        log.info("家政员预约时段释放：scheduleId={}, {}", scheduleId, ok ? "成功" : "无需释放");
        return ok;
    }

    /** C端-查询某家政员所有可约时段 */
    @Override
    @Cacheable(value = "housekeeperSchedule", key = "'available:' + #housekeeperId", sync = true)
    public List<HousekeeperSchedule> listAvailableByHousekeeperId(Long housekeeperId) {
        return mapper.getAvailableByHousekeeperId(housekeeperId);
    }
}
