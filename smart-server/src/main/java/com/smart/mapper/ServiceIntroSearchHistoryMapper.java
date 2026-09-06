package com.smart.mapper;

import com.smart.entity.ServiceIntroSearchHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ServiceIntroSearchHistoryMapper {

    @Insert("insert into service_intro_search_history (user_id, keyword, create_time) values (#{userId}, #{keyword}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ServiceIntroSearchHistory history);

    @Select("select * from service_intro_search_history where user_id = #{userId} order by create_time desc limit 10")
    List<ServiceIntroSearchHistory> listByUserId(Long userId);

    @Delete("delete from service_intro_search_history where user_id = #{userId}")
    void deleteByUserId(Long userId);

}
