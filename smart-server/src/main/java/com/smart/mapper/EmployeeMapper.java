package com.smart.mapper;

import com.smart.annotation.AutoFill;
import com.smart.dto.EmployeePageQueryDTO;
import com.smart.entity.Employee;
import com.smart.enumeration.OperationType;
import org.apache.ibatis.annotations.*;
import com.github.pagehelper.Page;

@Mapper
public interface EmployeeMapper {

    /**
     * 修改员工密码
     * @param employee
     */
    @AutoFill(OperationType.UPDATE) // 更新操作自动填充 updateTime、updateUser
    @Update("update employee set password = #{password}, update_time = #{updateTime}, update_user = #{updateUser} where id = #{id}")
    void editPassword(Employee employee);

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 新增员工
     * @param employee
     */
    @AutoFill(OperationType.INSERT) // 插入操作自动填充 createTime、updateTime、createUser、updateUser
    @Insert("insert into employee (username, name, password, phone, sex, age, id_number, status, role, create_time, update_time, create_user, update_user) values " +
            "(#{username}, #{name}, #{password}, #{phone}, #{sex}, #{age}, #{idNumber}, #{status}, #{role}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Employee employee);

    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用禁用员工账号
     * @param employee
     */
    @AutoFill(OperationType.UPDATE) // 更新操作自动填充 updateTime、updateUser
    @Update("update employee set status = #{status}, update_time = #{updateTime}, update_user = #{updateUser} where id = #{id}")
    void startOrStop(Employee employee);

    /**
     * 修改员工信息
     * @param employee
     */
    @AutoFill(OperationType.UPDATE) // 更新操作自动填充 updateTime、updateUser
    void update(Employee employee);

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    Employee getById(Long id);

    /**
     * 根据id删除员工
     * @param id
     */
    @Delete("delete from employee where id = #{id}")
    void delete(Long id);
}
