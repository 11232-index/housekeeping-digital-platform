package com.smart.mapper;

import com.smart.entity.HousekeeperSearchHistory;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface HousekeeperSearchHistoryMapper {

    @Insert("insert into housekeeper_search_history (user_id, keyword, create_time) values (#{userId}, #{keyword}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(HousekeeperSearchHistory history);

    @Select("select * from housekeeper_search_history where user_id = #{userId} order by create_time desc limit 10")
    List<HousekeeperSearchHistory> listByUserId(Long userId);

    @Delete("delete from housekeeper_search_history where user_id = #{userId}")
    void deleteByUserId(Long userId);

}
