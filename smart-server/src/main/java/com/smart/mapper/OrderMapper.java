package com.smart.mapper;

import com.github.pagehelper.Page;
import com.smart.entity.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @Insert("INSERT INTO orders (number, status, user_id, address_book_id, housekeeper_schedule_id, housekeeper_id, assign_time, remind_time, schedule_date, schedule_time, service_duration, order_time, " +
            "pay_time, pay_method, pay_status, amount, voucher_id, voucher_title, voucher_discount, discount_amount, remark, phone, address, contact_name, " +
            "cancel_reason, refund_reason, cancel_time, estimated_service_time, actual_service_time) " +
            "VALUES (#{number}, #{status}, #{userId}, #{addressBookId}, #{housekeeperScheduleId}, #{housekeeperId}, #{assignTime}, #{remindTime}, #{scheduleDate}, #{scheduleTime}, #{serviceDuration}, #{orderTime}, " +
            "#{payTime}, #{payMethod}, #{payStatus}, #{amount}, #{voucherId}, #{voucherTitle}, #{voucherDiscount}, #{discountAmount}, #{remark}, #{phone}, #{address}, " +
            "#{contactName}, #{cancelReason}, #{refundReason}, #{cancelTime}, #{estimatedServiceTime}, #{actualServiceTime})")
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
     * 用户申请退款（状态CAS / 乐观锁）：仅当订单属于该用户、仍处于“待付款(1)/待接单(2)”、
     * 且支付状态确为“已支付(1)”时，才置为“退款中(6)”并记录取消信息。
     * 返回影响行数：1=本次是首次成功申请；0=订单归属/状态/支付状态已变化（重复申请、越权、迟到申请均被拒）。
     */
    @Update("update orders set status = #{status}, cancel_reason = #{cancelReason}, cancel_time = now() " +
            "where id = #{id} and user_id = #{userId} and pay_status = #{payStatus} and status in (1, 2)")
    int refundByStatusCas(@Param("id") Long id,
                          @Param("userId") Long userId,
                          @Param("payStatus") Integer payStatus,
                          @Param("status") Integer status,
                          @Param("cancelReason") String cancelReason);

    /**
     * 用户支付订单（状态CAS / 乐观锁）：仅当订单属于该用户、且仍处于“待付款(1)”时，
     * 才置为“待接单(2) + 已支付(1)”并记录支付方式、付款时间。
     * 返回影响行数：1=本次支付成功；0=订单归属/状态已变化（重复支付、越权支付均被拒）。
     */
    @Update("update orders set status = #{status}, pay_status = #{payStatus}, " +
            "pay_method = #{payMethod}, pay_time = now() " +
            "where id = #{id} and user_id = #{userId} and status = #{pendingPayment}")
    int payByStatusCas(@Param("id") Long id,
                       @Param("userId") Long userId,
                       @Param("pendingPayment") Integer pendingPayment,
                       @Param("status") Integer status,
                       @Param("payStatus") Integer payStatus,
                       @Param("payMethod") Integer payMethod);

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

    /**
     * 查询超时未支付的订单（status=1 待付款 且 order_time 早于指定时间）
     * @param time 截止时间（格式 yyyy-MM-dd HH:mm:ss，字符串比较与 pageQuery 一致，避免时间精度/时区问题）
     * @return 超时未支付订单列表
     */
    @Select("SELECT * FROM orders WHERE status = 1 AND order_time < #{time}")
    List<Order> listTimeoutUnpaidOrders(@Param("time") String time);

    /**
     * 查询指定服务日(含)之前仍处于“服务中”的订单（凌晨巡检用）
     * @param day 截止服务日
     * @return 待自动完成的订单列表
     */
    @Select("SELECT * FROM orders WHERE status = 4 AND schedule_date IS NOT NULL AND schedule_date <= #{day}")
    List<Order> listInServiceOnOrBefore(@Param("day") LocalDate day);

    /**
     * 自动取消超时未支付订单（状态CAS：仅当仍为“待付款(1)”时置为“已取消(7)”）
     * @return 影响行数：1=本次成功取消；0=订单状态已变化（如已支付），被拒
     */
    @Update("UPDATE orders SET status = #{cancelled}, cancel_reason = #{reason}, cancel_time = now() " +
            "WHERE id = #{id} AND status = #{pendingPayment}")
    int autoCancelTimeoutById(@Param("id") Long id,
                              @Param("pendingPayment") Integer pendingPayment,
                              @Param("cancelled") Integer cancelled,
                              @Param("reason") String reason);

    /**
     * 自动完成超期“服务中”订单（状态CAS：仅当仍为“服务中(4)”时置为“已完成(5)”）
     * @return 影响行数：1=本次成功完成；0=订单状态已变化，被拒
     */
    @Update("UPDATE orders SET status = #{completed} WHERE id = #{id} AND status = #{inService}")
    int autoCompleteInServiceById(@Param("id") Long id,
                                  @Param("inService") Integer inService,
                                  @Param("completed") Integer completed);

    // ==================== 接单 / 指派 / 拒单 / 退款 ====================

    /**
     * 指派家政员（状态CAS）：仅当订单仍为「待接单(2)」且尚未指派时，写入家政员与指派时刻。
     * 状态不变（仍停在待接单，等家政员确认）；指派时刻用于「一级提醒」计时。
     * 返回影响行数：1=指派成功；0=状态已变化或已被指派（并发双指派被拒）。
     */
    @Update("update orders set housekeeper_id = #{housekeeperId}, assign_time = now() " +
            "where id = #{id} and status = #{pendingAccept} and housekeeper_id is null")
    int assignCas(@Param("id") Long id,
                  @Param("housekeeperId") Long housekeeperId,
                  @Param("pendingAccept") Integer pendingAccept);

    /**
     * 清空订单关联的排期id。
     * <p>
     * 用在「家政员订单换人重派」时：原来那位家政员的预约时段已经还回去了，
     * 订单不该再指向它（否则后续取消/退款会误释放一条已经不属于本单的时段）。
     */
    @Update("update orders set housekeeper_schedule_id = null where id = #{id}")
    int clearScheduleId(@Param("id") Long id);

    /**
     * 标记「一级提醒」已推送（幂等，防止定时任务每分钟重复推送）
     */
    @Update("update orders set remind_time = now() where id = #{id}")
    int markReminded(@Param("id") Long id);

    /**
     * 家政员接单（状态CAS）：仅当订单指派给该家政员、且仍为「待接单(2)」时置为「已接单(3)」，
     * 同时记录家政员填写的预计到达时间（写入 estimated_service_time）。
     * 返回影响行数：1=接单成功；0=状态已变化或不是指派给该家政员的订单
     */
    @Update("update orders set status = #{accepted}, estimated_service_time = #{estimatedArrivalTime} " +
            "where id = #{id} and housekeeper_id = #{housekeeperId} and status = #{pendingAccept}")
    int workerAcceptCas(@Param("id") Long id,
                        @Param("housekeeperId") Long housekeeperId,
                        @Param("pendingAccept") Integer pendingAccept,
                        @Param("accepted") Integer accepted,
                        @Param("estimatedArrivalTime") LocalDateTime estimatedArrivalTime);

    /**
     * 家政员不接单（状态CAS）：<b>退回「待接单(2)」而不是转待退款</b>。
     * 清空指派（housekeeper_id / assign_time / remind_time）让订单能被重新指派，
     * 拒单原因记在 cancel_reason 里供员工端在「待接单」列表看到；
     * 之后由员工端决定是重新指派给别的家政员，还是取消订单转待退款。
     * 状态不变（仍是 2），返回影响行数：1=成功；0=状态已变化或不是指派给该家政员的订单
     */
    @Update("update orders set housekeeper_id = null, assign_time = null, remind_time = null, " +
            "cancel_reason = #{reason}, cancel_time = now() " +
            "where id = #{id} and housekeeper_id = #{housekeeperId} and status = #{pendingAccept}")
    int workerRejectCas(@Param("id") Long id,
                        @Param("housekeeperId") Long housekeeperId,
                        @Param("pendingAccept") Integer pendingAccept,
                        @Param("reason") String reason);

    /**
     * 家政员确认到达（状态CAS）：已接单(3) → 服务中(4)，记录实际上门时间
     */
    @Update("update orders set status = #{inService}, actual_service_time = now() " +
            "where id = #{id} and housekeeper_id = #{housekeeperId} and status = #{accepted}")
    int workerArriveCas(@Param("id") Long id,
                        @Param("housekeeperId") Long housekeeperId,
                        @Param("accepted") Integer accepted,
                        @Param("inService") Integer inService);

    /**
     * 家政员完成服务（状态CAS）：服务中(4) → 已完成(5)
     */
    @Update("update orders set status = #{completed} " +
            "where id = #{id} and housekeeper_id = #{housekeeperId} and status = #{inService}")
    int workerCompleteCas(@Param("id") Long id,
                          @Param("housekeeperId") Long housekeeperId,
                          @Param("inService") Integer inService,
                          @Param("completed") Integer completed);

    /**
     * 员工端取消订单（状态CAS）：待接单(2)/已接单(3)/服务中(4) 均可取消，统一置为「待退款(6)」并记录原因。
     * 用于中途出现突发情况、订单无法继续完成时终止订单。
     * 返回影响行数：1=成功；0=状态已变化（已进入终态或被他人处理）
     */
    @Update("<script>update orders set status = #{refunding}, cancel_reason = #{reason}, cancel_time = now() " +
            "where id = #{id} and status in " +
            "<foreach collection='statuses' item='s' open='(' separator=',' close=')'>#{s}</foreach>" +
            "</script>")
    int staffCancelToRefundCas(@Param("id") Long id,
                               @Param("statuses") List<Integer> statuses,
                               @Param("refunding") Integer refunding,
                               @Param("reason") String reason);

    /**
     * 家政员报告「无法服务」（状态CAS）：已接单(3)/服务中(4) → 待退款(6)。
     * 与「不接单」区分：不接单是把订单退回待接单等重新指派，无法服务是接了之后出现突发情况，
     * 订单已实际上路、只能终止走退款。
     */
    @Update("<script>update orders set status = #{refunding}, cancel_reason = #{reason}, cancel_time = now() " +
            "where id = #{id} and housekeeper_id = #{housekeeperId} and status in " +
            "<foreach collection='statuses' item='s' open='(' separator=',' close=')'>#{s}</foreach>" +
            "</script>")
    int workerCannotServeCas(@Param("id") Long id,
                             @Param("housekeeperId") Long housekeeperId,
                             @Param("statuses") List<Integer> statuses,
                             @Param("refunding") Integer refunding,
                             @Param("reason") String reason);

    /**
     * 超时未接单（状态CAS）：置为「待退款(6)」，原因固定为「超时未接单」
     * 额外限定 pay_status=1，避免把未支付的订单也推进去
     */
    @Update("update orders set status = #{refunding}, cancel_reason = #{reason}, cancel_time = now() " +
            "where id = #{id} and status = #{pendingAccept} and pay_status = 1")
    int timeoutToRefundCas(@Param("id") Long id,
                           @Param("pendingAccept") Integer pendingAccept,
                           @Param("refunding") Integer refunding,
                           @Param("reason") String reason);

    /**
     * 员工端确认退款（状态CAS）：待退款(6) → 已取消(7)，记录退款原因，
     * 并把支付状态置为「退款(2)」（PAY_STATUS_REFUND 首次真正落库）
     */
    @Update("update orders set status = #{cancelled}, refund_reason = #{refundReason}, " +
            "pay_status = #{payStatusRefund}, cancel_time = now() " +
            "where id = #{id} and status = #{refunding}")
    int completeRefundCas(@Param("id") Long id,
                          @Param("refunding") Integer refunding,
                          @Param("cancelled") Integer cancelled,
                          @Param("payStatusRefund") Integer payStatusRefund,
                          @Param("refundReason") String refundReason);

    /**
     * 员工端推进订单状态（状态CAS）：白名单由服务层保证（3→4、4→5），不接受调用方任意传 status。
     * 进入「服务中(4)」时顺带记录实际上门时间。
     */
    @Update("update orders set status = #{to}, " +
            "actual_service_time = case when #{to} = 4 then now() else actual_service_time end " +
            "where id = #{id} and status = #{from}")
    int statusCasWithActualTime(@Param("id") Long id,
                                @Param("from") Integer from,
                                @Param("to") Integer to);

    /**
     * 定向更新预计上门时间（替代「读出整行 → set 一个字段 → 整体 update」的写法，
     * 后者会把并发写入的 pay_status / cancel_reason 原样覆盖回去）
     */
    @Update("update orders set estimated_service_time = #{time} where id = #{id}")
    int updateEstimatedTime(@Param("id") Long id, @Param("time") LocalDateTime time);

    /**
     * 查询所有「待接单(2) 且已支付(1)」的订单，供定时任务做超时与提醒判断。
     * 时间比较放在 Java 侧做 —— schedule_time 是字符串（如 "8:00~9:00"），SQL 里没法可靠比较。
     */
    @Select("SELECT * FROM orders WHERE status = 2 AND pay_status = 1 ORDER BY schedule_date")
    List<Order> listPendingAcceptOrders();

    /**
     * 查出某天「还存在未完成订单」的家政员及其时段（待接单/已接单/服务中 三种活跃状态）。
     * <p>
     * 用于指派候选时判断冲突：服务订单<b>不进 housekeeper_schedule</b>（那张表是家政员预约专用的），
     * 所以"这个家政员这个时段是不是已经有别的单了"只能从订单本身判断。
     * 时段比较放 Java 侧做 —— schedule_time 是字符串（"8:00~9:00" 与 "09:00~11:00" 混存），SQL 里比不可靠。
     */
    @Select("select id, housekeeper_id, schedule_time from orders " +
            "where schedule_date = #{date} and housekeeper_id is not null and status in (2, 3, 4)")
    List<Order> listActiveOrdersOnDate(@Param("date") LocalDate date);

    // ==================== 家政员端 ====================

    /**
     * 家政员端-分页查询指派给该家政员的订单（配合 PageHelper 使用）
     */
    @Select("<script>select * from orders where housekeeper_id = #{housekeeperId}" +
            "<if test='status != null'> and status = #{status}</if>" +
            " order by order_time desc</script>")
    Page<Order> pageQueryByHousekeeper(@Param("housekeeperId") Long housekeeperId,
                                       @Param("status") Integer status);

    /**
     * 家政员端-待接单数量（小程序角标）
     */
    @Select("select count(*) from orders where housekeeper_id = #{housekeeperId} and status = 2")
    long countPendingByHousekeeper(@Param("housekeeperId") Long housekeeperId);
}
