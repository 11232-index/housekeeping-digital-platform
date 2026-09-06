package com.smart.mapper;

import com.smart.entity.UpdatesSearchHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UpdatesSearchHistoryMapper {

    @Insert("insert into updates_search_history (user_id, keyword, create_time) values (#{userId}, #{keyword}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(UpdatesSearchHistory history);

    @Select("select * from updates_search_history where user_id = #{userId} order by create_time desc limit 10")
    List<UpdatesSearchHistory> listByUserId(Long userId);

    @Delete("delete from updates_search_history where user_id = #{userId}")
    void deleteByUserId(Long userId);

}
