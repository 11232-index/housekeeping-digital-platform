package com.smart.mapper;

import com.smart.annotation.AutoFill;
import com.smart.dto.UpdatesPageQueryDTO;
import com.smart.entity.Updates;
import com.smart.enumeration.OperationType;
import org.apache.ibatis.annotations.*;
import com.github.pagehelper.Page;

@Mapper
public interface UpdatesMapper {

    /**
     * 新增动态消息
     * @param updates
     */
    @AutoFill(OperationType.INSERT)
    @Insert("insert into updates (title, picture, content, create_time, update_time, create_user, update_user) values " +
            "(#{title}, #{picture}, #{content}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Updates updates);

    /**
     * 分页查询动态消息
     * @param updatesPageQueryDTO
     * @return
     */
    Page<Updates> pageQuery(UpdatesPageQueryDTO updatesPageQueryDTO);

    /**
     * 根据id查询动态消息
     * @param id
     * @return
     */
    @Select("select * from updates where id = #{id}")
    Updates getById(Long id);

    /**
     * 修改动态消息
     * @param updates
     */
    @AutoFill(OperationType.UPDATE)
    void update(Updates updates);

    /**
     * 根据id删除动态消息
     * @param id
     */
    @Delete("delete from updates where id = #{id}")
    void delete(Long id);

}
