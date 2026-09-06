package com.smart.mapper;

import com.smart.entity.Feed;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface FeedMapper {

    /** 新增feed（只存外键+时间） */
    @Insert("insert into feed (updates_id, service_intro_id, create_time, update_time) " +
            "values (#{updatesId}, #{serviceIntroId}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Feed feed);

    /** 删除指定updates关联的feed */
    @Delete("delete from feed where updates_id = #{updatesId}")
    void deleteByUpdatesId(Long updatesId);

    /** 删除指定service_intro关联的feed */
    @Delete("delete from feed where service_intro_id = #{serviceIntroId}")
    void deleteByServiceIntroId(Long serviceIntroId);

    /** 查询feed列表（UNION ALL联表，按时间降序） */
    List<Feed> list(@Param("limit") int limit);

}
