package com.smart.mapper;

import com.smart.entity.UserNotify;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户订单消息 数据访问层
 */
@Mapper
public interface UserNotifyMapper {

    /**
     * 新增一条用户消息
     */
    @Insert("INSERT INTO user_notify (user_id, type, title, content, order_id, order_number, read_flag, create_time) " +
            "VALUES (#{userId}, #{type}, #{title}, #{content}, #{orderId}, #{orderNumber}, #{readFlag}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserNotify notify);

    /**
     * 某用户最近 N 条消息（按 id 倒序）
     */
    @Select("SELECT * FROM user_notify WHERE user_id = #{userId} ORDER BY id DESC LIMIT #{limit}")
    List<UserNotify> listRecent(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 某用户的未读消息数
     */
    @Select("SELECT COUNT(*) FROM user_notify WHERE user_id = #{userId} AND read_flag = 0")
    long countUnread(@Param("userId") Long userId);

    /**
     * 标记某条消息已读（带 user_id 条件，只能改自己的消息）
     */
    @Update("UPDATE user_notify SET read_flag = 1 WHERE id = #{id} AND user_id = #{userId}")
    int markRead(@Param("userId") Long userId, @Param("id") Long id);

    /**
     * 一键全部已读
     */
    @Update("UPDATE user_notify SET read_flag = 1 WHERE user_id = #{userId} AND read_flag = 0")
    int markAllRead(@Param("userId") Long userId);
}
