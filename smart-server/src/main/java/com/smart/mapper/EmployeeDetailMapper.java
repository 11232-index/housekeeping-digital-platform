package com.smart.mapper;

import com.smart.annotation.AutoFill;
import com.smart.entity.EmployeeDetail;
import com.smart.enumeration.OperationType;
import org.apache.ibatis.annotations.*;

@Mapper
public interface EmployeeDetailMapper {

    @Select("select * from employee_detail where employee_id = #{employeeId}")
    EmployeeDetail getByEmployeeId(Long employeeId);

    @AutoFill(OperationType.INSERT) // 插入操作自动填充 createTime、updateTime
    @Insert("insert into employee_detail (employee_id, education, past_experience, position, salary, work_status, contract, health_certificate, create_time, update_time) " +
            "values (#{employeeId}, #{education}, #{pastExperience}, #{position}, #{salary}, #{workStatus}, #{contract}, #{healthCertificate}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(EmployeeDetail employeeDetail);

    @AutoFill(OperationType.UPDATE) // 更新操作自动填充 updateTime
    void update(EmployeeDetail employeeDetail);

    @Delete("delete from employee_detail where employee_id = #{employeeId}")
    void deleteByEmployeeId(Long employeeId);
}
