package com.smart.mapper;

import com.smart.annotation.AutoFill;
import com.smart.dto.HotPicksPageQueryDTO;
import com.smart.entity.HotPicks;
import com.smart.enumeration.OperationType;
import org.apache.ibatis.annotations.*;
import com.github.pagehelper.Page;

import java.util.List;

@Mapper
public interface HotPicksMapper {

    /**
     * 新增服务推荐
     */
    @AutoFill(OperationType.INSERT)
    @Insert("insert into hot_picks (name, picture, status, sort_order, create_time, update_time, create_user, update_user) values " +
            "(#{name}, #{picture}, #{status}, #{sortOrder}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(HotPicks hotPicks);

    /**
     * 分页查询服务推荐
     */
    Page<HotPicks> pageQuery(HotPicksPageQueryDTO dto);

    /**
     * 根据id查询
     */
    @Select("select * from hot_picks where id = #{id}")
    HotPicks getById(Long id);

    /**
     * 修改服务推荐
     */
    @AutoFill(OperationType.UPDATE)
    void update(HotPicks hotPicks);

    /**
     * 启用/禁用
     */
    @AutoFill(OperationType.UPDATE)
    @Update("update hot_picks set status = #{status}, update_time = #{updateTime}, update_user = #{updateUser} where id = #{id}")
    void startOrStop(HotPicks hotPicks);

    /**
     * 删除
     */
    @Delete("delete from hot_picks where id = #{id}")
    void delete(Long id);

    /**
     * 查询启用的服务推荐（最多5条，按排序+创建时间）
     */
    @Select("select * from hot_picks where status = 1 order by sort_order desc, create_time desc limit 5")
    List<HotPicks> listEnabled();

}
