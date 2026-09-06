package com.smart.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.smart.constant.MessageConstant;
import com.smart.context.BaseContext;
import com.smart.dto.OrderCancelDTO;
import com.smart.dto.OrderDTO;
import com.smart.dto.OrderItemDTO;
import com.smart.dto.OrderPageQueryDTO;
import com.smart.dto.OrderSubmitDTO;
import com.smart.dto.ShoppingCartDTO;
import com.smart.entity.AddressBook;
import com.smart.entity.Order;
import com.smart.entity.OrderDetail;
import com.smart.entity.ShoppingCart;
import com.smart.exception.ServiceOrderBusinessException;
import com.smart.mapper.OrderDetailMapper;
import com.smart.mapper.OrderMapper;
import com.smart.service.AddressBookService;
import com.smart.service.OrderService;
import com.smart.service.HousekeeperScheduleService;
import com.smart.service.ShoppingCartService;
import com.smart.vo.OrderSubmitVO;
import com.smart.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 * <p>
 * Redis缓存策略：
 * - 读：@Cacheable(value = "order", key = "'one:' + #id") 优先从Redis获取，未命中查MySQL并写入Redis
 * - 写：@CacheEvict(value = "order", key = "'one:' + #orderId") 精准清除单条order缓存
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    /** 订单号前缀 */
    private static final String ORDER_NO_PREFIX = "HS";

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private HousekeeperScheduleService housekeeperScheduleService;

    // ==================== 用户端接口 ====================

    /**
     * 用户提交订单
     * 流程：校验购物车 → 校验地址 → 构建订单+明细 → 批量插入 → 清空购物车
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrderSubmitDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户提交订单：userId={}, dto={}", userId, dto);

        // 1. 校验购物车
        List<ShoppingCart> cartList = shoppingCartService.list(userId);
        if (cartList == null || cartList.isEmpty()) {
            throw new ServiceOrderBusinessException(MessageConstant.BOOKING_LIST_IS_NULL);
        }

        // 2. 校验地址
        AddressBook addressBook = addressBookService.getById(dto.getAddressBookId());
        if (addressBook == null) {
            throw new ServiceOrderBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        // 3. 构建订单
        Order order = Order.builder()
                .number(generateOrderNumber())
                .status(Order.STATUS_PENDING_PAYMENT)
                .userId(userId)
                .addressBookId(dto.getAddressBookId())
                .housekeeperScheduleId(dto.getHousekeeperScheduleId())
                .scheduleDate(dto.getScheduleDate() != null ? java.time.LocalDate.parse(dto.getScheduleDate()) : null)
                .scheduleTime(dto.getScheduleTime())
                .serviceDuration(dto.getServiceDuration())
                .orderTime(LocalDateTime.now())
                .payMethod(dto.getPayMethod())
                .payStatus(Order.PAY_STATUS_UNPAID)
                .remark(dto.getRemark())
                .phone(addressBook.getPhone())
                .address(addressBook.getDetail())
                .contactName(addressBook.getConsignee())
                .estimatedServiceTime(buildEstimatedTime(dto.getScheduleDate(), dto.getScheduleTime()))
                .build();

        // 4. 从购物车构建明细并计算总金额
        List<OrderDetail> detailList = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (ShoppingCart cartItem : cartList) {
            OrderDetail detail = OrderDetail.builder()
                    .serviceItemId(cartItem.getServiceItemId())
                    .servicePackageId(cartItem.getServicePackageId())
                    .name(cartItem.getName())
                    .image(cartItem.getImage())
                    .requirement(cartItem.getRequirement())
                    .amount(cartItem.getAmount())
                    .build();
            totalAmount = totalAmount.add(cartItem.getAmount());
            detailList.add(detail);
        }
        order.setAmount(totalAmount);

        // 5. 保存订单（自增id回填到order.id）
        orderMapper.insert(order);
        log.info("订单创建成功：id={}, number={}, amount={}", order.getId(), order.getNumber(), order.getAmount());

        // 6. 设置订单id并批量插入明细
        for (OrderDetail detail : detailList) {
            detail.setOrderId(order.getId());
        }
        orderDetailMapper.insertBatch(detailList);
        log.info("订单明细批量插入成功：订单id={}, 共{}条", order.getId(), detailList.size());

        // 7. 清空购物车
        shoppingCartService.clean(userId);

        // 8. 返回结果
        return buildSubmitVO(order);
    }

    /**
     * 用户提交单条服务预约（仅一条明细，删除购物车对应项）
     */
    @Override
    @Transactional
    public OrderSubmitVO submitSingleOrder(OrderSubmitDTO dto, OrderItemDTO item) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户提交单条预约：userId={}, item={}", userId, item);

        // 1. 校验地址
        AddressBook addressBook = addressBookService.getById(dto.getAddressBookId());
        if (addressBook == null) {
            throw new ServiceOrderBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        // 2. 构建订单
        Order order = Order.builder()
                .number(generateOrderNumber())
                .status(Order.STATUS_PENDING_PAYMENT)
                .userId(userId)
                .addressBookId(dto.getAddressBookId())
                .housekeeperScheduleId(dto.getHousekeeperScheduleId())
                .scheduleDate(dto.getScheduleDate() != null ? LocalDate.parse(dto.getScheduleDate()) : null)
                .scheduleTime(dto.getScheduleTime())
                .serviceDuration(dto.getServiceDuration())
                .orderTime(LocalDateTime.now())
                .payMethod(dto.getPayMethod())
                .payStatus(Order.PAY_STATUS_UNPAID)
                .amount(item.getAmount())
                .remark(dto.getRemark())
                .phone(dto.getPhone() != null ? dto.getPhone() : addressBook.getPhone())
                .address(dto.getAddress() != null ? dto.getAddress() : addressBook.getDetail())
                .contactName(dto.getContactName() != null ? dto.getContactName() : addressBook.getConsignee())
                .estimatedServiceTime(buildEstimatedTime(dto.getScheduleDate(), dto.getScheduleTime()))
                .build();

        // 2.5 家政员预约：乐观抢位（先占时段、再建单；抢不到直接失败，避免白生成订单号/脏单）
        // 仅当该时段当前仍是“可约(status=1)”才会占用成功：并发下先到者成功，后到者因 WHERE status=1 影响 0 行被拒
        if (dto.getHousekeeperScheduleId() != null
                && !housekeeperScheduleService.tryOccupy(dto.getHousekeeperScheduleId())) {
            throw new ServiceOrderBusinessException(MessageConstant.HOUSEKEEPER_SCHEDULE_ALREADY_BOOKED);
        }

        // 3. 保存订单
        orderMapper.insert(order);
        log.info("订单创建成功：id={}, number={}, amount={}", order.getId(), order.getNumber(), order.getAmount());

        // 4. 插入单条明细
        OrderDetail detail = OrderDetail.builder()
                .orderId(order.getId())
                .housekeeperId(item.getHousekeeperId())
                .serviceItemId(item.getServiceItemId())
                .servicePackageId(item.getServicePackageId())
                .name(item.getName())
                .image(item.getImage())
                .requirement(item.getRequirement())
                .amount(item.getAmount())
                .build();
        orderDetailMapper.insert(detail);

        // 5. 删除购物车对应项（家政员预约不走购物车，跳过）
        ShoppingCartDTO cartDTO = new ShoppingCartDTO();
        if (item.getServiceItemId() != null) {
            cartDTO.setServiceItemId(item.getServiceItemId());
        } else if (item.getServicePackageId() != null) {
            cartDTO.setServicePackageId(item.getServicePackageId());
        }
        if (item.getRequirement() != null) {
            cartDTO.setRequirement(item.getRequirement());
        }
        if (cartDTO.getServiceItemId() != null || cartDTO.getServicePackageId() != null) {
            shoppingCartService.sub(cartDTO, userId);
        }

        // 6. 返回结果
        return buildSubmitVO(order);
    }

    private OrderSubmitVO buildSubmitVO(Order order) {
        return OrderSubmitVO.builder()
                .orderId(order.getId())
                .orderNumber(order.getNumber())
                .amount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();
    }

    /**
     * 用户端分页查询自己的订单
     */
    @Override
    public Page<OrderVO> pageQuery(OrderPageQueryDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户端订单分页查询：userId={}, dto={}", userId, dto);

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("status", dto.getStatus());
        params.put("number", dto.getNumber());
        params.put("beginTime", dto.getBeginTime());
        params.put("endTime", dto.getEndTime());

        return doPageQuery(dto, params);
    }

    /**
     * 根据id查询订单详情（含明细列表）
     * Redis缓存：key = "one:订单id"，查不到时缓存空值（防缓存穿透）
     */
    @Override
    @Cacheable(value = "order", key = "'one:' + #id", sync = true)
    public OrderVO getOrderById(Long id) {
        log.info("查询订单详情（缓存未命中，查MySQL）：id={}", id);

        Order order = orderMapper.getById(id);
        if (order == null) {
            // 订单不存在返回 null，配合全局"允许缓存 null"策略把空值写入缓存（order 为 5 分钟短 TTL），
            // 同一 ID 的重复请求命中缓存直接返回，不再穿透到 MySQL。
            // 业务错误（"服务订单不存在"）由 Controller 根据 null 统一返回，前端行为与之前抛异常一致。
            return null;
        }
        return toOrderVO(order);
    }

    /**
     * 用户取消订单（仅待付款/待接单状态可取消）
     */
    @Override
    @Transactional
    @CacheEvict(value = "order", key = "'one:' + #dto.orderId")
    public void cancelOrder(OrderCancelDTO dto) {
        log.info("用户取消订单：dto={}", dto);
        Long userId = BaseContext.getCurrentId();

        Order order = orderMapper.getById(dto.getOrderId());
        if (order == null) {
            throw new ServiceOrderBusinessException(MessageConstant.SERVICE_ORDER_NOT_FOUND);
        }
        // 订单状态机校验：仅“待付款(1)/待接单(2)”允许用户取消
        if (!Order.STATUS_PENDING_PAYMENT.equals(order.getStatus())
                && !Order.STATUS_PENDING_ACCEPT.equals(order.getStatus())) {
            throw new ServiceOrderBusinessException(MessageConstant.SERVICE_ORDER_STATUS_ERROR);
        }

        // 已支付 → 走“退款中”，订单未真正取消，其占用的家政员时段暂不释放
        if (Order.PAY_STATUS_PAID.equals(order.getPayStatus())) {
            order.setStatus(Order.STATUS_REFUNDING);
            order.setCancelReason(dto.getCancelReason());
            orderMapper.update(order);
            log.info("订单进入退款中：id={}, reason={}", dto.getOrderId(), dto.getCancelReason());
            return;
        }

        // 未支付 → 已取消。用带条件的 UPDATE（订单状态CAS）保证“同一订单只能取消成功一次”：
        // 并发/重复/迟到的第二次取消会因订单状态已不在 (1,2) 而影响 0 行，直接被拒，不会再去碰时段
        int rows = orderMapper.cancelByStatusCas(
                dto.getOrderId(), userId, Order.STATUS_CANCELLED, dto.getCancelReason());
        if (rows == 0) {
            throw new ServiceOrderBusinessException(MessageConstant.SERVICE_ORDER_STATUS_ERROR);
        }

        // 取消成功后才释放该订单占用的家政员时段（status 0→1，同样带条件：重复释放/误放均无效）
        if (order.getHousekeeperScheduleId() != null) {
            housekeeperScheduleService.tryRelease(order.getHousekeeperScheduleId());
        }
        log.info("订单取消成功：id={}, reason={}", dto.getOrderId(), dto.getCancelReason());
    }

    /**
     * 用户支付订单（仅待付款状态可支付）
     */
    @Override
    @Transactional
    @CacheEvict(value = "order", key = "'one:' + #orderId")
    public void payOrder(Long orderId, Integer payMethod) {
        log.info("用户支付订单：id={}, payMethod={}", orderId, payMethod);

        Order order = orderMapper.getById(orderId);
        if (order == null) {
            throw new ServiceOrderBusinessException(MessageConstant.SERVICE_ORDER_NOT_FOUND);
        }
        if (!order.getStatus().equals(Order.STATUS_PENDING_PAYMENT)) {
            throw new ServiceOrderBusinessException(MessageConstant.SERVICE_ORDER_STATUS_ERROR);
        }

        order.setStatus(Order.STATUS_PENDING_ACCEPT);
        order.setPayStatus(Order.PAY_STATUS_PAID);
        order.setPayMethod(payMethod);
        order.setPayTime(LocalDateTime.now());
        orderMapper.update(order);
        log.info("订单支付成功：id={}", orderId);
    }

    // ==================== 管理端接口 ====================

    /**
     * 管理端分页查询所有订单
     */
    @Override
    public Page<OrderVO> adminPageQuery(OrderPageQueryDTO dto) {
        log.info("管理端订单分页查询：dto={}", dto);

        Map<String, Object> params = new HashMap<>();
        params.put("status", dto.getStatus());
        params.put("number", dto.getNumber());
        params.put("userId", dto.getUserId());
        params.put("contactName", dto.getContactName());
        params.put("beginTime", dto.getBeginTime());
        params.put("endTime", dto.getEndTime());

        return doPageQuery(dto, params);
    }

    /**
     * 管理端更新订单状态（接单/开始服务/完成）
     */
    @Override
    @Transactional
    @CacheEvict(value = "order", key = "'one:' + #orderId")
    public void updateOrderStatus(Long orderId, Integer status) {
        log.info("管理端更新订单状态：orderId={}, targetStatus={}", orderId, status);

        Order order = orderMapper.getById(orderId);
        if (order == null) {
            throw new ServiceOrderBusinessException(MessageConstant.SERVICE_ORDER_NOT_FOUND);
        }

        order.setStatus(status);
        // 开始服务时记录实际上门时间
        if (status.equals(Order.STATUS_IN_SERVICE)) {
            order.setActualServiceTime(LocalDateTime.now());
        }
        orderMapper.update(order);
        log.info("订单状态更新成功：orderId={}, newStatus={}", orderId, status);
    }

    /**
     * 管理端设置预计上门时间
     */
    @Override
    @Transactional
    @CacheEvict(value = "order", key = "'one:' + #orderId")
    public void updateEstimatedServiceTime(Long orderId, OrderDTO dto) {
        log.info("管理端设置预计上门时间：orderId={}, dto={}", orderId, dto);

        Order order = orderMapper.getById(orderId);
        if (order == null) {
            throw new ServiceOrderBusinessException(MessageConstant.SERVICE_ORDER_NOT_FOUND);
        }

        order.setEstimatedServiceTime(dto.getEstimatedServiceTime());
        orderMapper.update(order);
        log.info("预计上门时间设置成功：orderId={}, estimatedServiceTime={}", orderId, dto.getEstimatedServiceTime());
    }

    // ==================== 工具方法 ====================

    /**
     * 生成订单号：HS + yyyyMMddHHmmss + 4位随机数
     */
    /**
     * 根据预约日期+时段计算预计上门时间（取时段开始时间）
     * 如 scheduleDate=2024-08-05, scheduleTime=8:00~9:00 → 2024-08-05 08:00:00
     */
    private LocalDateTime buildEstimatedTime(String scheduleDate, String scheduleTime) {
        if (scheduleDate == null || scheduleTime == null) return null;
        try {
            String startTime = scheduleTime.split("~")[0].trim(); // "8:00"
            return LocalDateTime.of(
                    LocalDate.parse(scheduleDate),
                    LocalTime.parse(startTime)
            );
        } catch (Exception e) {
            log.warn("解析预计上门时间失败：date={}, time={}", scheduleDate, scheduleTime);
            return null;
        }
    }

    @Override
    public String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return ORDER_NO_PREFIX + timestamp + random;
    }

    /**
     * 执行分页查询（公共方法）
     */
    private Page<OrderVO> doPageQuery(OrderPageQueryDTO dto, Map<String, Object> params) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Order> orderPage = (Page<Order>) orderMapper.pageQuery(params);

        // 转换 Order → OrderVO（含明细列表）
        List<OrderVO> voList = orderPage.getResult().stream()
                .map(this::toOrderVO)
                .collect(Collectors.toList());

        Page<OrderVO> resultPage = new Page<>();
        BeanUtils.copyProperties(orderPage, resultPage, "result");
        resultPage.addAll(voList);
        return resultPage;
    }

    /**
     * Order 转 OrderVO，附带明细列表
     */
    private OrderVO toOrderVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        List<OrderDetail> detailList = orderDetailMapper.listByOrderId(order.getId());
        vo.setOrderDetailList(detailList);
        return vo;
    }
}
