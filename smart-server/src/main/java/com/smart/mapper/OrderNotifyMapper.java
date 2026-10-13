package com.smart.mapper;

import com.smart.entity.OrderNotify;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单提醒消息 数据访问层（含员工已读关系）
 */
@Mapper
public interface OrderNotifyMapper {

    /**
     * 新增一条订单提醒消息
     */
    @Insert("INSERT INTO order_notify (type, title, content, order_id, order_number, amount, create_time) " +
            "VALUES (#{type}, #{title}, #{content}, #{orderId}, #{orderNumber}, #{amount}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderNotify notify);

    /**
     * 最近 N 条消息（按 id 倒序）
     */
    @Select("SELECT * FROM order_notify ORDER BY id DESC LIMIT #{limit}")
    List<OrderNotify> listRecent(@Param("limit") int limit);

    /**
     * 某员工的未读消息数（order_notify 中不存在于该员工已读表的数据）
     */
    @Select("SELECT COUNT(*) FROM order_notify n WHERE NOT EXISTS " +
            "(SELECT 1 FROM order_notify_read r WHERE r.notify_id = n.id AND r.employee_id = #{employeeId})")
    long countUnread(@Param("employeeId") Long employeeId);

    /**
     * 某员工已读过的消息id集合
     */
    @Select("SELECT notify_id FROM order_notify_read WHERE employee_id = #{employeeId}")
    List<Long> listReadIds(@Param("employeeId") Long employeeId);

    /**
     * 标记某员工已读某条消息（重复标记忽略）
     */
    @Insert("INSERT IGNORE INTO order_notify_read (employee_id, notify_id, read_time) " +
            "VALUES (#{employeeId}, #{notifyId}, now())")
    int insertRead(@Param("employeeId") Long employeeId, @Param("notifyId") Long notifyId);

    /**
     * 一键全部已读（把当前所有消息标记为该员工已读）
     */
    @Insert("INSERT IGNORE INTO order_notify_read (employee_id, notify_id, read_time) " +
            "SELECT #{employeeId}, id, now() FROM order_notify")
    int markAllRead(@Param("employeeId") Long employeeId);
}
