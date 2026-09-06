package com.smart.mapper;

import com.smart.annotation.AutoFill;
import com.smart.entity.Administrator;
import com.smart.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdministratorMapper {

    /**
     * 根据用户名查询管理员
     * @param username
     * @return
     */
    @Select("select * from administrator where username = #{username}")
    Administrator getByUsername(String username);

    /**
     * 根据ID查询管理员
     * @param id
     * @return
     */
    @Select("select * from administrator where id = #{id}")
    Administrator getById(Long id);

    /**
     * 修改管理员密码
     * @param administrator
     */
    @AutoFill(OperationType.UPDATE) // 更新操作自动填充 updateTime、updateUser
    @Update("update administrator set password = #{password}, update_time = #{updateTime}, update_user = #{updateUser} where id = #{id}")
    void editPassword(Administrator administrator);

    /**
     * 新增管理员账号
     * @param administrator
     */
    @AutoFill(OperationType.INSERT) // 插入操作自动填充 createTime、updateTime、createUser、updateUser
    @Insert("insert into administrator (username, name, password, phone, sex, age, id_number, status, create_time, update_time, create_user, update_user) " +
            "values (#{username}, #{name}, #{password}, #{phone}, #{sex}, #{age}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Administrator administrator);

    /**
     * 修改管理员名称
     * @param updateAdmin
     */
    @AutoFill(OperationType.UPDATE) // 更新操作自动填充 updateTime、updateUser
    @Update("update administrator set name = #{name}, update_time = #{updateTime}, update_user = #{updateUser} where id = #{id}")
    void editName(Administrator updateAdmin);

    /**
     * 设置密保
     * @param administrator
     */
    @AutoFill(OperationType.UPDATE) // 更新操作自动填充 updateTime、updateUser
    @Update("update administrator set security_question = #{securityQuestion}, security_answer = #{securityAnswer}, update_time = #{updateTime}, update_user = #{updateUser} where id = #{id}")
    void updateSecurity(Administrator administrator);
}
