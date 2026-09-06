package com.smart.mapper;

import com.smart.entity.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 订单表数据访问层
 */
@Mapper
public interface OrderMapper {

    /**
     * 新增订单
     * @param order 订单实体
     */
    @Insert("INSERT INTO orders (number, status, user_id, address_book_id, housekeeper_schedule_id, schedule_date, schedule_time, service_duration, order_time, " +
            "pay_time, pay_method, pay_status, amount, remark, phone, address, contact_name, " +
            "cancel_reason, cancel_time, estimated_service_time, actual_service_time) " +
            "VALUES (#{number}, #{status}, #{userId}, #{addressBookId}, #{housekeeperScheduleId}, #{scheduleDate}, #{scheduleTime}, #{serviceDuration}, #{orderTime}, " +
            "#{payTime}, #{payMethod}, #{payStatus}, #{amount}, #{remark}, #{phone}, #{address}, " +
            "#{contactName}, #{cancelReason}, #{cancelTime}, #{estimatedServiceTime}, #{actualServiceTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Order order);

    /**
     * 根据id查询订单
     * @param id 订单id
     * @return 订单实体
     */
    @Select("SELECT * FROM orders WHERE id = #{id}")
    Order getById(Long id);

    /**
     * 根据订单号查询订单
     * @param number 订单号
     * @return 订单实体
     */
    @Select("SELECT * FROM orders WHERE number = #{number}")
    Order getByNumber(String number);

    /**
     * 用户取消订单（状态CAS / 乐观锁）：仅当订单属于该用户、且仍处于“待付款(1)/待接单(2)”时，
     * 才置为目标状态并记录取消信息。返回影响行数：1=本次是首次成功取消；0=订单状态已变化（重复/迟到取消被拒）。
     */
    @Update("update orders set status = #{status}, cancel_reason = #{cancelReason}, cancel_time = now() " +
            "where id = #{id} and user_id = #{userId} and status in (1, 2)")
    int cancelByStatusCas(@Param("id") Long id,
                          @Param("userId") Long userId,
                          @Param("status") Integer status,
                          @Param("cancelReason") String cancelReason);

    /**
     * 动态更新订单（只更新非空字段）
     * @param order 订单实体
     */
    void update(Order order);

    /**
     * 分页查询订单（动态条件）
     * @param params 查询参数
     * @return 订单列表
     */
    List<Order> pageQuery(Map<String, Object> params);
}
