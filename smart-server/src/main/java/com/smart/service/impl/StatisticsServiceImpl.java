package com.smart.service.impl;

import com.smart.exception.BaseException;
import com.smart.mapper.StatisticsMapper;
import com.smart.service.StatisticsService;
import com.smart.vo.DailyStatVO;
import com.smart.vo.OrderStatisticsVO;
import com.smart.vo.SalesRankItemVO;
import com.smart.vo.SalesRankVO;
import com.smart.vo.StatusCountVO;
import com.smart.vo.TurnoverStatisticsVO;
import com.smart.vo.UserStatisticsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端工作台 - 统计服务实现
 */
@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    /** 默认统计天数（不传日期区间时统计最近 30 天） */
    private static final int DEFAULT_DAYS = 30;
    /** 允许的最大统计天数，避免一次查询跨度过大 */
    private static final int MAX_DAYS = 366;

    /** 订单状态名称 */
    private static final Map<Integer, String> STATUS_NAME = Map.of(
            1, "待付款",
            2, "待接单",
            3, "已接单",
            4, "服务中",
            5, "已完成",
            6, "退款中",
            7, "已取消"
    );

    @Autowired
    private StatisticsMapper statisticsMapper;

    @Override
    public TurnoverStatisticsVO getTurnoverStatistics(String beginTime, String endTime) {
        DateRange range = normalizeRange(beginTime, endTime);
        List<String> dateList = buildDateList(range);

        // 每日营业额：查询结果只含有数据的日期，需要按日期补 0
        List<DailyStatVO> dailyList = statisticsMapper.sumTurnoverByDay(range.begin, range.end);
        Map<String, BigDecimal> turnoverMap = dailyList.stream()
                .filter(item -> item.getDate() != null)
                .collect(Collectors.toMap(DailyStatVO::getDate,
                        item -> item.getAmount() == null ? BigDecimal.ZERO : item.getAmount(),
                        (a, b) -> a));
        List<BigDecimal> turnoverList = dateList.stream()
                .map(date -> turnoverMap.getOrDefault(date, BigDecimal.ZERO))
                .collect(Collectors.toList());

        BigDecimal totalTurnover = statisticsMapper.sumTurnover(range.begin, range.end);
        if (totalTurnover == null) {
            totalTurnover = BigDecimal.ZERO;
        }
        Long validOrderCount = statisticsMapper.countValidOrders(range.begin, range.end);
        if (validOrderCount == null) {
            validOrderCount = 0L;
        }
        // 平均客单价 = 总营业额 / 有效订单数，无有效订单时为 0
        BigDecimal avgOrderAmount = validOrderCount == 0
                ? BigDecimal.ZERO
                : totalTurnover.divide(BigDecimal.valueOf(validOrderCount), 2, RoundingMode.HALF_UP);

        log.info("营业额统计：{} ~ {}，总营业额 {}，有效订单 {} 单", range.begin, range.end, totalTurnover, validOrderCount);

        return TurnoverStatisticsVO.builder()
                .totalTurnover(totalTurnover)
                .validOrderCount(validOrderCount)
                .avgOrderAmount(avgOrderAmount)
                .dateList(dateList)
                .turnoverList(turnoverList)
                .build();
    }

    @Override
    public UserStatisticsVO getUserStatistics(String beginTime, String endTime) {
        DateRange range = normalizeRange(beginTime, endTime);
        List<String> dateList = buildDateList(range);

        List<DailyStatVO> dailyList = statisticsMapper.countNewUsersByDay(range.begin, range.end);
        Map<String, Long> newUserMap = dailyList.stream()
                .filter(item -> item.getDate() != null)
                .collect(Collectors.toMap(DailyStatVO::getDate,
                        item -> item.getCount() == null ? 0L : item.getCount(),
                        (a, b) -> a));
        List<Long> newUserList = dateList.stream()
                .map(date -> newUserMap.getOrDefault(date, 0L))
                .collect(Collectors.toList());

        Long totalUser = statisticsMapper.countTotalUsers();
        if (totalUser == null) {
            totalUser = 0L;
        }
        // 区间新增用户数 = 每日新增之和
        long newUser = newUserList.stream().mapToLong(Long::longValue).sum();

        log.info("用户统计：{} ~ {}，总用户 {}，新增用户 {}", range.begin, range.end, totalUser, newUser);

        return UserStatisticsVO.builder()
                .totalUser(totalUser)
                .newUser(newUser)
                .dateList(dateList)
                .newUserList(newUserList)
                .build();
    }

    @Override
    public OrderStatisticsVO getOrderStatistics(String beginTime, String endTime) {
        DateRange range = normalizeRange(beginTime, endTime);
        List<String> dateList = buildDateList(range);

        List<DailyStatVO> dailyList = statisticsMapper.countOrdersByDay(range.begin, range.end);
        Map<String, Long> orderMap = dailyList.stream()
                .filter(item -> item.getDate() != null)
                .collect(Collectors.toMap(DailyStatVO::getDate,
                        item -> item.getCount() == null ? 0L : item.getCount(),
                        (a, b) -> a));
        List<Long> orderList = dateList.stream()
                .map(date -> orderMap.getOrDefault(date, 0L))
                .collect(Collectors.toList());

        List<StatusCountVO> statusList = new ArrayList<>();
        long validOrder = 0L;
        long cancelledOrder = 0L;
        for (StatusCountVO item : statisticsMapper.countOrdersByStatus(range.begin, range.end)) {
            item.setName(STATUS_NAME.getOrDefault(item.getStatus(), "未知"));
            if (item.getCount() == null) {
                item.setCount(0L);
            }
            statusList.add(item);
            if (Integer.valueOf(5).equals(item.getStatus())) {
                validOrder += item.getCount();
            }
            // 退款中(6) + 已取消(7) 都算取消
            if (Integer.valueOf(6).equals(item.getStatus()) || Integer.valueOf(7).equals(item.getStatus())) {
                cancelledOrder += item.getCount();
            }
        }

        Long totalOrder = statisticsMapper.countOrders(range.begin, range.end);
        if (totalOrder == null) {
            totalOrder = 0L;
        }

        log.info("订单统计：{} ~ {}，订单总数 {}，有效订单 {}，取消订单 {}",
                range.begin, range.end, totalOrder, validOrder, cancelledOrder);

        return OrderStatisticsVO.builder()
                .totalOrder(totalOrder)
                .validOrder(validOrder)
                .cancelledOrder(cancelledOrder)
                .dateList(dateList)
                .orderList(orderList)
                .statusList(statusList)
                .build();
    }

    @Override
    public SalesRankVO getSalesRank(String beginTime, String endTime) {
        DateRange range = normalizeRange(beginTime, endTime);
        List<SalesRankItemVO> items = statisticsMapper.listTopSales(range.begin, range.end);

        List<String> nameList = new ArrayList<>();
        List<Long> numberList = new ArrayList<>();
        List<BigDecimal> amountList = new ArrayList<>();
        for (SalesRankItemVO item : items) {
            nameList.add(item.getName());
            numberList.add(item.getNumber() == null ? 0L : item.getNumber());
            amountList.add(item.getAmount() == null ? BigDecimal.ZERO : item.getAmount());
        }

        log.info("销量排名统计：{} ~ {}，共 {} 项", range.begin, range.end, nameList.size());

        return SalesRankVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .amountList(amountList)
                .build();
    }

    /**
     * 校验并补全日期区间：不传时默认最近 30 天，只传一端时补另一端
     */
    private DateRange normalizeRange(String beginTime, String endTime) {
        LocalDate begin;
        LocalDate end;
        try {
            begin = isBlank(beginTime) ? null : LocalDate.parse(beginTime.trim());
            end = isBlank(endTime) ? null : LocalDate.parse(endTime.trim());
        } catch (DateTimeParseException e) {
            throw new BaseException("日期格式错误，应为 yyyy-MM-dd");
        }

        if (begin == null && end == null) {
            end = LocalDate.now();
            begin = end.minusDays(DEFAULT_DAYS - 1);
        } else if (begin == null) {
            begin = end.minusDays(DEFAULT_DAYS - 1);
        } else if (end == null) {
            end = begin.plusDays(DEFAULT_DAYS - 1);
        }

        if (begin.isAfter(end)) {
            throw new BaseException("开始日期不能晚于结束日期");
        }
        if (ChronoUnit.DAYS.between(begin, end) + 1 > MAX_DAYS) {
            throw new BaseException("统计区间不能超过" + MAX_DAYS + "天");
        }

        return new DateRange(begin, end);
    }

    /**
     * 生成区间内每一天的日期字符串（用于折线图补 0）
     */
    private List<String> buildDateList(DateRange range) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<String> dateList = new ArrayList<>();
        for (LocalDate date = range.beginDate; !date.isAfter(range.endDate); date = date.plusDays(1)) {
            dateList.add(date.format(formatter));
        }
        return dateList;
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 规范化后的日期区间
     */
    private static class DateRange {
        /** 起始日期（LocalDate，用于生成日期列表） */
        private final LocalDate beginDate;
        /** 结束日期（LocalDate，用于生成日期列表） */
        private final LocalDate endDate;
        /** 起始日期字符串（yyyy-MM-dd，用于 SQL 查询） */
        private final String begin;
        /** 结束日期字符串（yyyy-MM-dd，用于 SQL 查询） */
        private final String end;

        private DateRange(LocalDate beginDate, LocalDate endDate) {
            this.beginDate = beginDate;
            this.endDate = endDate;
            this.begin = beginDate.toString();
            this.end = endDate.toString();
        }
    }
}
