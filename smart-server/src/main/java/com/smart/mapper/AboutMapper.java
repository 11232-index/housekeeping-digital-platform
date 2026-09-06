package com.smart.mapper;

import com.smart.annotation.AutoFill;
import com.smart.dto.AboutPageQueryDTO;
import com.smart.entity.About;
import com.smart.enumeration.OperationType;
import org.apache.ibatis.annotations.*;
import com.github.pagehelper.Page;

@Mapper
public interface AboutMapper {

    /**
     * 新增关于我们
     */
    @AutoFill(OperationType.INSERT)
    @Insert("insert into about (title, sort_order, content, create_time, update_time, create_user, update_user) values " +
            "(#{title}, #{sortOrder}, #{content}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(About about);

    /**
     * 分页查询
     */
    Page<About> pageQuery(AboutPageQueryDTO dto);

    /**
     * 根据id查询
     */
    @Select("select * from about where id = #{id}")
    About getById(Long id);

    /**
     * 修改
     */
    @AutoFill(OperationType.UPDATE)
    void update(About about);

    /**
     * 删除
     */
    @Delete("delete from about where id = #{id}")
    void delete(Long id);

}
