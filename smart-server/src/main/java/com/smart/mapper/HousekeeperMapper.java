package com.smart.mapper;

import com.smart.annotation.AutoFill;
import com.smart.dto.HousekeeperPageQueryDTO;
import com.smart.entity.Housekeeper;
import com.smart.enumeration.OperationType;
import org.apache.ibatis.annotations.*;
import com.github.pagehelper.Page;

import java.util.List;

@Mapper
public interface HousekeeperMapper {

    /**
     * 新增家政员（员工role=cleaner时自动调用）
     */
    @AutoFill(OperationType.INSERT)
    @Insert("insert into housekeeper (employee_id, name, picture, price, rating, `rank`, carousel, review, create_user, update_user, create_time, update_time) values " +
            "(#{employeeId}, #{name}, #{picture}, #{price}, #{rating}, #{rank}, #{carousel}, #{review}, #{createUser}, #{updateUser}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Housekeeper housekeeper);

    /**
     * 分页查询家政员
     */
    Page<Housekeeper> pageQuery(HousekeeperPageQueryDTO dto);

    /**
     * 根据id查询
     */
    @Select("select * from housekeeper where id = #{id}")
    Housekeeper getById(Long id);

    /**
     * 根据员工id查询
     */
    @Select("select * from housekeeper where employee_id = #{employeeId}")
    Housekeeper getByEmployeeId(Long employeeId);

    /**
     * 修改家政员
     */
    @AutoFill(OperationType.UPDATE)
    void update(Housekeeper housekeeper);

    /**
     * 根据员工id删除家政员（员工role从cleaner改为其他时调用）
     */
    @Delete("delete from housekeeper where employee_id = #{employeeId}")
    void deleteByEmployeeId(Long employeeId);

}
