package com.smart.task;

import com.smart.entity.Order;
import com.smart.mapper.OrderMapper;
import com.smart.service.HousekeeperScheduleService;
import com.smart.service.OrderNotifyService;
import com.smart.service.UserNotifyService;
import com.smart.utils.ScheduleTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 定时任务类：定时处理订单
 * <p>
 * 1. 每分钟扫描一次：将“超时未支付”的订单自动取消（释放占用的排班时段）
 * 2. 每天凌晨 1 点扫描一次：将“前一工作日仍处于服务中”的订单自动置为已完成
 * <p>
 * 说明：取消/完成均使用带状态条件的 UPDATE（CAS），保证并发/重复触发不会误操作；
 * 超时时长与状态判断参考 {@link Order} 常量（1待付款 2待接单 3已接单 4服务中 5已完成 7已取消）。
 */
@Component
@Slf4j
public class OrderTask {

    /** 待付款订单超时时长（分钟）：下单后超过该时长仍未支付，系统自动取消 */
    private static final int UNPAID_TIMEOUT_MINUTES = 15;

    /** 自动取消原因 */
    private static final String CANCEL_REASON_TIMEOUT = "订单超时";

    /** 超时未接单转入待退款时记录的取消原因 */
    private static final String CANCEL_REASON_ACCEPT_TIMEOUT = "超时未接单";

    /** 一级提醒：员工指派后超过该分钟数仍未被确认，就提醒员工去催（可配，演示/实际使用可放宽） */
    @Value("${smart.order.accept-remind-minutes:30}")
    private int acceptRemindMinutes;

    /** 二级超时：距「服务开始时刻」不足该小时数仍无人接单，就转入待退款（可配） */
    @Value("${smart.order.accept-timeout-hours:2}")
    private int acceptTimeoutHours;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private HousekeeperScheduleService housekeeperScheduleService;

    @Autowired
    private OrderNotifyService orderNotifyService;

    @Autowired
    private UserNotifyService userNotifyService;

    @Autowired
    private CacheManager cacheManager;

    /**
     * 处理超时订单：每分钟触发一次
     */
    @Scheduled(fixedRate = 60_000) //每分钟触发一次
    public void processExpiredOrders() {
        // 时间按字符串 yyyy-MM-dd HH:mm:ss 传给 SQL，与现有分页查询一致，避免 LocalDateTime 纳秒精度导致的 SQL 语法问题
        String deadline = LocalDateTime.now().minusMinutes(UNPAID_TIMEOUT_MINUTES)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("定时处理超时订单开始，扫描待付款且早于 {} 的订单：{}", deadline, LocalDateTime.now());

        List<Order> expiredList = orderMapper.listTimeoutUnpaidOrders(deadline);
        if (expiredList == null || expiredList.isEmpty()) {
            log.info("定时处理超时订单：无超时未支付订单");
            return;
        }

        int successCount = 0;
        for (Order order : expiredList) {
            // 状态CAS：仅当仍是“待付款(1)”才取消成功，避免把已支付的订单误取消
            int rows = orderMapper.autoCancelTimeoutById(
                    order.getId(),
                    Order.STATUS_PENDING_PAYMENT,
                    Order.STATUS_CANCELLED,
                    CANCEL_REASON_TIMEOUT);
            if (rows > 0) {
                successCount++;
                // 取消成功后才释放该订单占用的家政员时段（CAS，重复/误放无效）
                if (order.getHousekeeperScheduleId() != null) {
                    housekeeperScheduleService.tryRelease(order.getHousekeeperScheduleId());
                }
                // 直接改库不会自动清 Spring Cache，这里手动清掉订单详情缓存，避免详情仍显示“待付款”
                evictOrderCache(order.getId());
                // 通知下单用户：单子因为没付款被自动取消了，得让用户知道
                userNotifyService.push(order.getUserId(), "cancelled", "订单已取消",
                        "您的订单 " + order.getNumber() + " 因超时未支付已自动取消",
                        order.getId(), order.getNumber());
                log.info("超时未支付订单已自动取消：id={}, number={}", order.getId(), order.getNumber());
            } else {
                log.info("超时未支付订单状态已变化，跳过：id={}, number={}", order.getId(), order.getNumber());
            }
        }
        log.info("定时处理超时订单结束，共处理 {} 单", successCount);
    }

    /**
     * 处理一直服务中的订单：每天凌晨 1 点触发一次
     * 将“前一工作日（跳过周末）仍处于服务中”的订单自动置为已完成
     */
    @Scheduled(cron = "0 0 1 * * ?") //每凌晨一点触发一次
    public void handleStuckInServiceOrders() {
        LocalDate previousWorkday = getPreviousWorkday(LocalDate.now());
        log.info("定时处理服务中订单开始，扫描服务日早于等于 {} 的服务中订单：{}", previousWorkday, LocalDateTime.now());

        List<Order> stuckList = orderMapper.listInServiceOnOrBefore(previousWorkday);
        if (stuckList == null || stuckList.isEmpty()) {
            log.info("定时处理服务中订单：无超期服务中订单");
            return;
        }

        int successCount = 0;
        for (Order order : stuckList) {
            // 状态CAS：仅当仍是“服务中(4)”才置为“已完成(5)”
            int rows = orderMapper.autoCompleteInServiceById(
                    order.getId(),
                    Order.STATUS_IN_SERVICE,
                    Order.STATUS_COMPLETED);
            if (rows > 0) {
                successCount++;
                // 直接改库不会自动清 Spring Cache，这里手动清掉订单详情缓存，避免详情仍显示“服务中”
                evictOrderCache(order.getId());
                log.info("前一工作日超期服务中订单已自动完成：id={}, number={}", order.getId(), order.getNumber());
            } else {
                log.info("服务中订单状态已变化，跳过：id={}, number={}", order.getId(), order.getNumber());
            }
        }
        log.info("定时处理服务中订单结束，共处理 {} 单", successCount);
    }

    /**
     * 待接单订单的两级兜底：每分钟触发一次
     * <p>
     * 1. <b>一级提醒</b>：员工指派后超过 {@code acceptRemindMinutes} 分钟仍未被家政员确认 →
     *    推一条提醒到员工端消息中心（<b>不改状态</b>），让商家打电话跟进。
     *    用 remind_time 做幂等标记，避免每分钟重复刷屏。
     * 2. <b>二级超时</b>：距「服务开始时刻」不足 {@code acceptTimeoutHours} 小时仍无人接单 →
     *    转入「待退款(6)」并释放已占用时段。
     * <p>
     * 时间比较必须在 Java 侧做：schedule_time 是自由文本（"8:00~9:00" 与 "09:00~11:00" 在库里混存），
     * SQL 里没法可靠比较；且计时基准是「预约时段的前半段」而不是下单时刻，
     * 这样不要求家政员盯着手机抢时间响应。
     */
    @Scheduled(fixedRate = 60_000)
    public void processPendingAcceptOrders() {
        List<Order> list = orderMapper.listPendingAcceptOrders();
        if (list == null || list.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        int reminded = 0;
        int timedOut = 0;

        for (Order order : list) {
            try {
                // ---------- 一级：提醒员工去催（只针对「员工指派」的订单，用户指定的订单没有指派时刻） ----------
                if (order.getRemindTime() == null
                        && order.getAssignTime() != null
                        && order.getAssignTime().plusMinutes(acceptRemindMinutes).isBefore(now)) {
                    // 先落库标记再推送：即使推送抛异常，也不会下一分钟再推一次
                    orderMapper.markReminded(order.getId());
                    orderNotifyService.push("acceptRemind", "家政员未确认接单",
                            "订单 " + order.getNumber() + " 指派后 " + acceptRemindMinutes + " 分钟仍未确认，请电话跟进",
                            order.getId(), order.getNumber(), order.getAmount());
                    reminded++;
                }

                // ---------- 二级：临近服务仍未接单 → 待退款 ----------
                LocalDateTime serviceStart = ScheduleTimeUtil.parseStart(
                        order.getScheduleDate(), order.getScheduleTime());
                if (serviceStart == null) {
                    // 时段解析不出来就没法判断，记日志留人工介入，不做自动退款
                    log.warn("待接单订单时段无法解析，跳过超时判断：id={}, scheduleDate={}, scheduleTime={}",
                            order.getId(), order.getScheduleDate(), order.getScheduleTime());
                    continue;
                }
                if (serviceStart.minusHours(acceptTimeoutHours).isBefore(now)) {
                    int rows = orderMapper.timeoutToRefundCas(order.getId(),
                            Order.STATUS_PENDING_ACCEPT, Order.STATUS_REFUNDING, CANCEL_REASON_ACCEPT_TIMEOUT);
                    if (rows > 0) {
                        timedOut++;
                        // 刻意不释放预约时段：这单是因为没人接才黄的，
                        // 放回「可约」只会让别的用户再约到同一个没人能做的时段
                        evictOrderCache(order.getId());
                        orderNotifyService.push("refund", "订单超时待退款",
                                "订单 " + order.getNumber() + " 距服务开始不足 " + acceptTimeoutHours
                                        + " 小时仍无人接单，已转入待退款",
                                order.getId(), order.getNumber(), order.getAmount());
                        // 通知下单用户：单子没人接，要退款了
                        userNotifyService.push(order.getUserId(), "timeout", "订单超时",
                                "您的订单 " + order.getNumber() + " 因无人接单已转入退款处理",
                                order.getId(), order.getNumber());
                        log.info("待接单订单超时未接单，已转入待退款：id={}, number={}", order.getId(), order.getNumber());
                    }
                }
            } catch (Exception e) {
                // 单条失败不影响其他订单
                log.error("处理待接单订单失败：id={}, {}", order.getId(), e.getMessage());
            }
        }

        if (reminded > 0 || timedOut > 0) {
            log.info("待接单订单巡检：提醒 {} 单，转待退款 {} 单", reminded, timedOut);
        }
    }

    /**
     * 清除订单详情缓存（order::one:{orderId}），避免定时任务直接改库后读到旧状态
     */
    private void evictOrderCache(Long orderId) {
        try {
            Cache cache = cacheManager.getCache("order");
            if (cache != null) {
                cache.evict("one:" + orderId);
            }
        } catch (Exception e) {
            log.error("清除订单缓存失败：id={}, {}", orderId, e.getMessage());
        }
    }

    /**
     * 获取前一工作日（今天向前推，跳过周六、周日）
     */
    private LocalDate getPreviousWorkday(LocalDate today) {
        LocalDate day = today.minusDays(1);
        while (day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY) {
            day = day.minusDays(1);
        }
        return day;
    }
}
