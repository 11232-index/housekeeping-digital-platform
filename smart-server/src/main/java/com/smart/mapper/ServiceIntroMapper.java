package com.smart.mapper;

import com.smart.annotation.AutoFill;
import com.smart.dto.ServiceIntroPageQueryDTO;
import com.smart.entity.ServiceIntro;
import com.smart.enumeration.OperationType;
import org.apache.ibatis.annotations.*;
import com.github.pagehelper.Page;

@Mapper
public interface ServiceIntroMapper {

    /**
     * 新增服务介绍
     */
    @AutoFill(OperationType.INSERT)
    @Insert("insert into service_intro (title, picture, content, create_time, update_time, create_user, update_user) values " +
            "(#{title}, #{picture}, #{content}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ServiceIntro serviceIntro);

    /**
     * 分页查询服务介绍
     */
    Page<ServiceIntro> pageQuery(ServiceIntroPageQueryDTO dto);

    /**
     * 根据id查询服务介绍
     */
    @Select("select * from service_intro where id = #{id}")
    ServiceIntro getById(Long id);

    /**
     * 修改服务介绍
     */
    @AutoFill(OperationType.UPDATE)
    void update(ServiceIntro serviceIntro);

    /**
     * 根据id删除服务介绍
     */
    @Delete("delete from service_intro where id = #{id}")
    void delete(Long id);

}
