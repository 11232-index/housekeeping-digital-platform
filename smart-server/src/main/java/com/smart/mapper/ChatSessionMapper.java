package com.smart.mapper;

import com.smart.annotation.AutoFill;
import com.smart.entity.ChatSession;
import com.smart.enumeration.OperationType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChatSessionMapper {

    @AutoFill(OperationType.INSERT) // 插入操作自动填充 createTime、updateTime
    @Insert("insert into chat_session (admin_id, title, create_time, update_time) " +
            "values (#{adminId}, #{title}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ChatSession chatSession);

    @AutoFill(OperationType.UPDATE) // 更新操作自动填充 updateTime
    @Update("update chat_session set title = #{title}, update_time = #{updateTime} where id = #{id}")
    void update(ChatSession chatSession);

    @Delete("delete from chat_session where id = #{id}")
    void deleteById(Long id);

    @Select("select * from chat_session where id = #{id}")
    ChatSession getById(Long id);

    List<ChatSession> pageQuery(Long adminId, String title);

    @Delete("delete from chat_session where admin_id = #{adminId}")
    void deleteAllByAdminId(Long adminId);
}
