package com.smart.mapper;

import com.smart.entity.SearchHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SearchHistoryMapper {

    /**
     * 新增搜索记录
     */
    @Insert("insert into search_history (user_id, keyword, create_time) values (#{userId}, #{keyword}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SearchHistory searchHistory);

    /**
     * 查询用户搜索记录（最新10条）
     */
    @Select("select * from search_history where user_id = #{userId} order by create_time desc limit 10")
    List<SearchHistory> listByUserId(Long userId);

    /**
     * 清空用户搜索记录
     */
    @Delete("delete from search_history where user_id = #{userId}")
    void deleteByUserId(Long userId);

}
