package com.smart.mapper;

import com.smart.annotation.AutoFill;
import com.smart.dto.HousekeeperSchedulePageQueryDTO;
import com.smart.entity.HousekeeperSchedule;
import com.smart.enumeration.OperationType;
import org.apache.ibatis.annotations.*;
import com.github.pagehelper.Page;

import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface HousekeeperScheduleMapper {

    /** 新增预约时间 */
    @AutoFill(OperationType.INSERT)
    @Insert("insert into housekeeper_schedule (housekeeper_id, schedule_date, start_time, end_time, status, create_user, update_user, create_time, update_time) " +
            "values (#{housekeeperId}, #{scheduleDate}, #{startTime}, #{endTime}, #{status}, #{createUser}, #{updateUser}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(HousekeeperSchedule schedule);

    /** 分页查询 */
    Page<HousekeeperSchedule> pageQuery(HousekeeperSchedulePageQueryDTO dto);

    /** 根据id查询 */
    @Select("select * from housekeeper_schedule where id = #{id}")
    HousekeeperSchedule getById(Long id);

    /** 修改预约时间 */
    @AutoFill(OperationType.UPDATE)
    void update(HousekeeperSchedule schedule);

    /** 根据id删除 */
    @Delete("delete from housekeeper_schedule where id = #{id}")
    void delete(Long id);

    /** 根据家政员id删除所有预约时间 */
    @Delete("delete from housekeeper_schedule where housekeeper_id = #{housekeeperId}")
    void deleteByHousekeeperId(Long housekeeperId);

    /** 批量删除 */
    void deleteBatch(@Param("ids") List<Long> ids);

    /** 批量更新状态 */
    void updateStatusBatch(@Param("ids") List<Long> ids, @Param("status") Integer status);

    /** 乐观抢位：仅当当前为可约(1)时置为已占用(0)，返回影响行数（1成功 0已被约） */
    @Update("update housekeeper_schedule set status = 0 where id = #{id} and status = 1")
    int occupy(Long id);

    /** 乐观释放：仅当当前为已占用(0)时置为可约(1)，返回影响行数（1成功 0无需释放） */
    @Update("update housekeeper_schedule set status = 1 where id = #{id} and status = 0")
    int release(Long id);

    /** C端-查询某家政员所有可约时段（status=1） */
    @Select("select * from housekeeper_schedule where housekeeper_id = #{housekeeperId} and status = 1 order by schedule_date, start_time")
    List<HousekeeperSchedule> getAvailableByHousekeeperId(Long housekeeperId);

    /** 查询某家政员某天某时段是否已存在（防重复） */
    @Select("select * from housekeeper_schedule where housekeeper_id = #{housekeeperId} " +
            "and schedule_date = #{scheduleDate} and start_time = #{startTime} and end_time = #{endTime}")
    HousekeeperSchedule getByHousekeeperAndTime(@Param("housekeeperId") Long housekeeperId,
            @Param("scheduleDate") java.time.LocalDate scheduleDate,
            @Param("startTime") java.time.LocalTime startTime,
            @Param("endTime") java.time.LocalTime endTime);

}
