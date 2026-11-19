package com.smart.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.smart.constant.MessageConstant;
import com.smart.constant.OrderEventConstant;
import com.smart.context.BaseContext;
import com.smart.dto.OrderCancelDTO;
import com.smart.dto.OrderDTO;
import com.smart.dto.OrderItemDTO;
import com.smart.dto.OrderPageQueryDTO;
import com.smart.dto.OrderSubmitDTO;
import com.smart.dto.ShoppingCartDTO;
import com.smart.entity.AddressBook;
import com.smart.entity.Housekeeper;
import com.smart.entity.HousekeeperSchedule;
import com.smart.entity.Order;
import com.smart.entity.OrderDetail;
import com.smart.entity.ShoppingCart;
import com.smart.entity.UserVoucher;
import com.smart.entity.Voucher;
import com.smart.exception.ServiceOrderBusinessException;
import com.smart.mapper.HousekeeperMapper;
import com.smart.mapper.OrderDetailMapper;
import com.smart.mapper.OrderMapper;
import com.smart.mapper.UserVoucherMapper;
import com.smart.mapper.VoucherMapper;
import com.smart.mq.OrderDelayPublisher;
import com.smart.mq.OrderEventPublisher;
import com.smart.service.AddressBookService;
import com.smart.service.OrderService;
import com.smart.service.HousekeeperScheduleService;
import com.smart.service.ShoppingCartService;
import com.smart.utils.OrderStatusUtil;
import com.smart.utils.ScheduleTimeUtil;
import com.smart.vo.HousekeeperCandidateVO;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    /** 员工端可取消的订单状态：待接单 / 已接单 / 服务中（中途出现突发情况时终止订单） */
    private static final List<Integer> CANCELABLE_STATUSES = Arrays.asList(
            Order.STATUS_PENDING_ACCEPT, Order.STATUS_ACCEPTED, Order.STATUS_IN_SERVICE);

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

    @Autowired
    private HousekeeperMapper housekeeperMapper;

    @Autowired
    private VoucherMapper voucherMapper;

    @Autowired
    private UserVoucherMapper userVoucherMapper;

    @Autowired
    private OrderEventPublisher orderEventPublisher;

    @Autowired
    private OrderDelayPublisher orderDelayPublisher;

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
        // 4.5 用券：购物车这条路只会有服务项目/套餐，属于「服务预约」，允许用券
        applyVoucher(userId, order, totalAmount, dto.getVoucherId());

        // 5. 保存订单（自增id回填到order.id）
        orderMapper.insert(order);
        log.info("订单创建成功：id={}, number={}, amount={}", order.getId(), order.getNumber(), order.getAmount());

        // 5.5 用券的订单：把券置为已使用（状态CAS，并发下同一张券只会被用掉一次）
        consumeVoucher(userId, order);

        // 6. 设置订单id并批量插入明细
        for (OrderDetail detail : detailList) {
            detail.setOrderId(order.getId());
        }
        orderDetailMapper.insertBatch(detailList);
        log.info("订单明细批量插入成功：订单id={}, 共{}条", order.getId(), detailList.size());

        // 7. 清空购物车
        shoppingCartService.clean(userId);

        // 7.5 写订单事件到本地消息表（与订单同事务提交）
        orderEventPublisher.publish(OrderEventConstant.CREATED, order);
        orderDelayPublisher.scheduleUnpaidCancel(order);
        if (order.getScheduleDate() != null && order.getScheduleTime() != null) {
            orderDelayPublisher.scheduleAcceptCheck(order);
        }

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
                // 家政员预约：把「这单该谁接」冗余写到订单级字段，方便家政员端查「我的订单」。
                // 订单类型的区分仍然只看 order_detail.housekeeper_id，不依赖这一列。
                .housekeeperId(item.getHousekeeperId())
                .remark(dto.getRemark())
                .phone(dto.getPhone() != null ? dto.getPhone() : addressBook.getPhone())
                .address(dto.getAddress() != null ? dto.getAddress() : addressBook.getDetail())
                .contactName(dto.getContactName() != null ? dto.getContactName() : addressBook.getConsignee())
                .build();

        // 2.5 家政员预约：乐观抢位（先占时段、再建单；抢不到直接失败，避免白生成订单号/脏单）
        // 仅当该时段当前仍是“可约(status=1)”才会占用成功：并发下先到者成功，后到者因 WHERE status=1 影响 0 行被拒
        if (dto.getHousekeeperScheduleId() != null
                && !housekeeperScheduleService.tryOccupy(dto.getHousekeeperScheduleId())) {
            throw new ServiceOrderBusinessException(MessageConstant.HOUSEKEEPER_SCHEDULE_ALREADY_BOOKED);
        }

        // 2.6 用券：只有「服务预约」能用 —— item 里带 housekeeperId 说明这是家政员预约，直接不用券
        applyVoucher(userId, order, item.getAmount(),
                item.getHousekeeperId() != null ? null : dto.getVoucherId());

        // 3. 保存订单
        orderMapper.insert(order);
        log.info("订单创建成功：id={}, number={}, amount={}", order.getId(), order.getNumber(), order.getAmount());

        // 3.5 用券的订单：把券置为已使用（状态CAS，并发下同一张券只会被用掉一次）
        consumeVoucher(userId, order);

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

        // 5.5 写订单事件到本地消息表（与订单同事务提交）
        orderEventPublisher.publish(OrderEventConstant.CREATED, order);
        orderDelayPublisher.scheduleUnpaidCancel(order);
        if (order.getScheduleDate() != null && order.getScheduleTime() != null) {
            orderDelayPublisher.scheduleAcceptCheck(order);
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
        // 归属校验：只允许操作自己的订单；他人的订单一律按“不存在”返回，避免泄露订单归属
        if (userId == null || !userId.equals(order.getUserId())) {
            throw new ServiceOrderBusinessException(MessageConstant.SERVICE_ORDER_NOT_FOUND);
        }
        // 订单状态机校验：仅“待付款(1)/待接单(2)”允许用户取消
        if (!Order.STATUS_PENDING_PAYMENT.equals(order.getStatus())
                && !Order.STATUS_PENDING_ACCEPT.equals(order.getStatus())) {
            throw new ServiceOrderBusinessException(MessageConstant.SERVICE_ORDER_STATUS_ERROR);
        }

        // 已支付 → 走“待退款(6)”，并立即释放占用的家政员时段
        // 同样用带条件的 UPDATE（含 user_id / pay_status / status）收口：并发或重复申请只会成功一次，
        // 也不存在改到他人订单的可能（虽然上面已校验归属，这里再兜一层，语义与未支付分支保持一致）
        if (Order.PAY_STATUS_PAID.equals(order.getPayStatus())) {
            int rows = orderMapper.refundByStatusCas(
                    dto.getOrderId(), userId, Order.PAY_STATUS_PAID,
                    Order.STATUS_REFUNDING, dto.getCancelReason());
            if (rows == 0) {
                throw new ServiceOrderBusinessException(MessageConstant.SERVICE_ORDER_STATUS_ERROR);
            }
            // 用户主动取消：跟「家政员能不能做」无关，时段放回「可约」让别人还能约。
            // 这是唯一会自动释放的订单终结方式（另一处是待付款超时取消），
            // 拒单 / 员工取消 / 改派 / 退款 / 超时无人接 都刻意不释放 —— 见各自方法的注释。
            if (order.getHousekeeperScheduleId() != null) {
                housekeeperScheduleService.tryRelease(order.getHousekeeperScheduleId());
            }
            // 通知由 MQ 消费者统一处理，这里只负责改状态
            order.setStatus(Order.STATUS_REFUNDING);
            orderEventPublisher.publish(OrderEventConstant.CANCELLED, order, null, dto.getCancelReason(), null, "user");
            log.info("订单进入待退款：id={}, reason={}", dto.getOrderId(), dto.getCancelReason());
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
        order.setStatus(Order.STATUS_CANCELLED);
        orderEventPublisher.publish(OrderEventConstant.CANCELLED, order, null, dto.getCancelReason(), null, "user");
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

        Long userId = BaseContext.getCurrentId();

        Order order = orderMapper.getById(orderId);
        if (order == null) {
            throw new ServiceOrderBusinessException(MessageConstant.SERVICE_ORDER_NOT_FOUND);
        }
        // 归属校验：只允许支付自己的订单；他人的订单一律按“不存在”返回，避免泄露订单归属
        if (userId == null || !userId.equals(order.getUserId())) {
            throw new ServiceOrderBusinessException(MessageConstant.SERVICE_ORDER_NOT_FOUND);
        }
        if (!order.getStatus().equals(Order.STATUS_PENDING_PAYMENT)) {
            throw new ServiceOrderBusinessException(MessageConstant.SERVICE_ORDER_STATUS_ERROR);
        }

        // 带条件更新（CAS）：仅当订单仍属于该用户且仍是“待付款(1)”时才置为“待接单 + 已支付”，
        // 并发/重复付款会因状态已不在 (1) 而影响 0 行被拒，不会重复置位
        int rows = orderMapper.payByStatusCas(
                orderId, userId, Order.STATUS_PENDING_PAYMENT,
                Order.STATUS_PENDING_ACCEPT, Order.PAY_STATUS_PAID, payMethod);
        if (rows == 0) {
            throw new ServiceOrderBusinessException(MessageConstant.SERVICE_ORDER_STATUS_ERROR);
        }
        order.setStatus(Order.STATUS_PENDING_ACCEPT);
        order.setPayStatus(Order.PAY_STATUS_PAID);
        order.setPayMethod(payMethod);
        order.setPayTime(LocalDateTime.now());
        orderEventPublisher.publish(OrderEventConstant.PAID, order);
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
        params.put("phone", dto.getPhone());
        params.put("beginTime", dto.getBeginTime());
        params.put("endTime", dto.getEndTime());
        // 订单类型过滤：service=服务订单，housekeeper=家政员订单，null=全部
        params.put("orderType", dto.getOrderType());

        return doPageQuery(dto, params);
    }

    /**
     * 管理端推进订单状态（白名单：已接单(3)→服务中(4)、服务中(4)→已完成(5)）
     * <p>
     * 刻意不接受调用方任意指定 status：
     * 「待接单→已接单」要走指派接口（需要占用家政员时段），
     * 「转入待退款/已取消」也必须带原因，各有独立入口。
     * 若放开任意传值，就能绕过「必须填写原因」的需求，也能把订单直接置成终态。
     */
    @Override
    @Transactional
    @CacheEvict(value = "order", key = "'one:' + #orderId")
    public void updateOrderStatus(Long orderId, Integer status) {
        log.info("管理端推进订单状态：orderId={}, targetStatus={}", orderId, status);
        if (status == null) {
            throw new ServiceOrderBusinessException(MessageConstant.SERVICE_ORDER_STATUS_ERROR);
        }

        // 白名单：目标状态 → 允许的来源状态
        Integer from;
        if (Order.STATUS_IN_SERVICE.equals(status)) {
            from = Order.STATUS_ACCEPTED;          // 3 → 4 开始服务
        } else if (Order.STATUS_COMPLETED.equals(status)) {
            from = Order.STATUS_IN_SERVICE;        // 4 → 5 完成
        } else {
            throw new ServiceOrderBusinessException(MessageConstant.SERVICE_ORDER_STATUS_ERROR);
        }

        // 状态CAS：并发/重复点击/状态已变化时影响 0 行被拒；
        // 进入「服务中」时由 SQL 顺带记录实际上门时间
        int rows = orderMapper.statusCasWithActualTime(orderId, from, status);
        if (rows == 0) {
            throw new ServiceOrderBusinessException(stateChangedMessage(orderId));
        }
        Order order = orderMapper.getById(orderId);
        if (order != null) {
            order.setStatus(status);
            String eventType = Order.STATUS_IN_SERVICE.equals(status)
                    ? OrderEventConstant.ARRIVED : OrderEventConstant.COMPLETED;
            orderEventPublisher.publish(eventType, order);
        }
        log.info("订单状态推进成功：orderId={}, {}→{}", orderId, from, status);
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

        // 定向更新单个字段：原来的「读出整行 → 改一个字段 → 整体 update」会把读到的
        // pay_status / cancel_reason 等原样写回，并发下覆盖掉其它流程刚写入的值
        orderMapper.updateEstimatedTime(orderId, dto.getEstimatedServiceTime());
        log.info("预计上门时间设置成功：orderId={}, estimatedServiceTime={}", orderId, dto.getEstimatedServiceTime());
    }

    // ==================== 指派 / 取消 / 退款 ====================

    /**
     * 员工端查询可指派的家政员候选（该订单服务时段内没有已占用排期的家政员）
     */
    @Override
    public List<HousekeeperCandidateVO> listAssignCandidates(Long orderId) {
        Order order = orderMapper.getById(orderId);
        if (order == null) {
            throw new ServiceOrderBusinessException(MessageConstant.SERVICE_ORDER_NOT_FOUND);
        }
        if (!Order.STATUS_PENDING_ACCEPT.equals(order.getStatus())) {
            throw new ServiceOrderBusinessException(OrderStatusUtil.alreadyMessage(order.getStatus()));
        }
        LocalTime[] range = requireScheduleRange(order);

        // 1) 先排除「家政员预约表里已有重叠时段」的人 —— 那些时段已经被用户约走了。
        //    忽略订单自己关联的那条：拒单时已把它释放回「可约」，不忽略的话刚拒单的家政员
        //    会被自己这条记录挡住，出现「他没在忙却派不了他」的死结。
        List<HousekeeperCandidateVO> candidates = housekeeperMapper.listCandidates(
                order.getScheduleDate(), range[0], range[1], order.getHousekeeperScheduleId());

        // 2) 再排除「当天已经有别的未完成订单、且时段重叠」的人。
        //    服务订单不进预约表（那张表是家政员预约专用的），所以占用情况只能从订单本身看。
        List<Order> sameDayOrders = orderMapper.listActiveOrdersOnDate(order.getScheduleDate());
        Set<Long> busyHousekeeperIds = new HashSet<>();
        for (Order other : sameDayOrders) {
            if (other.getHousekeeperId() == null || other.getId().equals(orderId)) {
                continue;
            }
            if (overlaps(other.getScheduleTime(), range)) {
                busyHousekeeperIds.add(other.getHousekeeperId());
            }
        }
        if (busyHousekeeperIds.isEmpty()) {
            return candidates;
        }
        return candidates.stream()
                .filter(c -> !busyHousekeeperIds.contains(c.getHousekeeperId()))
                .collect(Collectors.toList());
    }

    /**
     * 判断某订单的时段文本是否与目标时段重叠。
     * 解析失败按「不重叠」处理 —— 宁可漏挡一个，也不要因为一条脏数据把所有人都排除掉。
     */
    private boolean overlaps(String timeRange, LocalTime[] target) {
        LocalTime[] range = ScheduleTimeUtil.parseRange(timeRange);
        if (range == null) {
            return false;
        }
        return range[0].isBefore(target[1]) && range[1].isAfter(target[0]);
    }

    /**
     * 员工端指派家政员：写入订单级家政员与指派时刻，并占用该家政员此时段。
     * 订单状态不变，仍停在「待接单(2)」等家政员确认。
     * <p>
     * 执行顺序不能反：先 CAS 抢订单、再占时段。反过来的话，并发中失败的一方
     * 会在排期表里留下一条永远释放不掉的孤儿占用。
     */
    @Override
    @Transactional
    @CacheEvict(value = "order", key = "'one:' + #orderId")
    public void assignOrder(Long orderId, Long housekeeperId) {
        log.info("员工端指派家政员：orderId={}, housekeeperId={}", orderId, housekeeperId);
        if (housekeeperId == null) {
            throw new ServiceOrderBusinessException(MessageConstant.HOUSEKEEPER_NOT_FOUND);
        }

        Order order = orderMapper.getById(orderId);
        if (order == null) {
            throw new ServiceOrderBusinessException(MessageConstant.SERVICE_ORDER_NOT_FOUND);
        }
        // 时段必须能解析，否则既没法筛候选、也没法判断冲突
        requireScheduleRange(order);

        // 1) CAS 抢订单：仅当仍是待接单且尚未指派时成功，并发双指派只有一个能过
        int rows = orderMapper.assignCas(orderId, housekeeperId, Order.STATUS_PENDING_ACCEPT);
        if (rows == 0) {
            throw new ServiceOrderBusinessException(stateChangedMessage(orderId));
        }

        // 2) 处理订单原有的预约时段（只有家政员订单会有；服务订单这里是 null）。
        //    派回原来那位 → 时段原封不动继续占着（拒单时就没释放，正好接着用）；
        //    换人了     → 只把订单上的关联清掉，原时段【保持不可约】不释放
        //                （家政员拒单 = 他那个时间做不了，放回去别的用户又会约到）。
        //    新指派产生的占用不写 housekeeper_schedule（那是家政员预约专用的表），
        //    由订单表记录、候选查询时判断，见 listAssignCandidates。
        if (order.getHousekeeperScheduleId() != null) {
            HousekeeperSchedule schedule = housekeeperScheduleService.getById(order.getHousekeeperScheduleId());
            if (schedule == null || !housekeeperId.equals(schedule.getHousekeeperId())) {
                orderMapper.clearScheduleId(orderId);
            }
        }

        // 3) 取家政员姓名（明细更新和通知都要用）
        Housekeeper housekeeper = housekeeperMapper.getById(housekeeperId);
        String hkName = housekeeper != null ? housekeeper.getName() : "家政员";

        // 4) 家政员订单被拒后换人：同步明细上的家政员姓名与id，避免详情里“家政员”和“服务明细”对不上。
        //    服务订单的明细 housekeeper_id 为空，这条 SQL 影响 0 行，不会误伤服务名。
        orderDetailMapper.updateHousekeeperInfo(orderId, housekeeperId, hkName);

        // 5) 通知由 MQ 消费者统一处理，这里只负责写状态和发事件
        order.setHousekeeperId(housekeeperId);
        orderEventPublisher.publish(OrderEventConstant.ASSIGNED, order, BaseContext.getCurrentId(), null, null, "employee");
        orderDelayPublisher.scheduleAcceptCheck(order);
        log.info("指派完成：orderId={}, housekeeperId={}", orderId, housekeeperId);
    }

    /**
     * 员工端取消待接单订单：置为待退款并记录取消原因，同时释放已占用时段
     */
    @Override
    @Transactional
    @CacheEvict(value = "order", key = "'one:' + #orderId")
    public void staffCancelOrder(Long orderId, String cancelReason) {
        log.info("员工端取消订单：orderId={}, reason={}", orderId, cancelReason);
        String reason = requireReason(cancelReason, MessageConstant.ORDER_CANCEL_REASON_REQUIRED);

        Order order = orderMapper.getById(orderId);
        if (order == null) {
            throw new ServiceOrderBusinessException(MessageConstant.SERVICE_ORDER_NOT_FOUND);
        }

        int rows = orderMapper.staffCancelToRefundCas(
                orderId, CANCELABLE_STATUSES, Order.STATUS_REFUNDING, reason);
        if (rows == 0) {
            throw new ServiceOrderBusinessException(stateChangedMessage(orderId));
        }

        // 刻意【不释放】预约时段：这类取消多半就是因为「家政员那个时间做不了」，
        // 放回「可约」只会让别的用户再约到同一个没人能做的时段。
        // 想重新开放的话去预约管理页手动改回「可约」。
        // 通知下单用户由 MQ 消费者统一处理
        order.setStatus(Order.STATUS_REFUNDING);
        orderEventPublisher.publish(OrderEventConstant.CANCELLED, order, BaseContext.getCurrentId(), reason, null, "employee");
        log.info("员工端取消成功，订单转入待退款：orderId={}", orderId);
    }

    /**
     * 员工端确认退款：待退款(6) → 已取消(7)，记录退款原因，支付状态置为「退款」
     */
    @Override
    @Transactional
    @CacheEvict(value = "order", key = "'one:' + #orderId")
    public void completeRefund(Long orderId, String refundReason) {
        log.info("员工端确认退款：orderId={}, reason={}", orderId, refundReason);
        String reason = requireReason(refundReason, MessageConstant.ORDER_REFUND_REASON_REQUIRED);

        Order order = orderMapper.getById(orderId);
        if (order == null) {
            throw new ServiceOrderBusinessException(MessageConstant.SERVICE_ORDER_NOT_FOUND);
        }

        int rows = orderMapper.completeRefundCas(
                orderId, Order.STATUS_REFUNDING, Order.STATUS_CANCELLED,
                Order.PAY_STATUS_REFUND, reason);
        if (rows == 0) {
            throw new ServiceOrderBusinessException(stateChangedMessage(orderId));
        }

        // 同样不释放时段：这单是因为做不了/没人接才退的款，时段保持不可约
        // 通知下单用户由 MQ 消费者统一处理
        order.setStatus(Order.STATUS_CANCELLED);
        order.setPayStatus(Order.PAY_STATUS_REFUND);
        orderEventPublisher.publish(OrderEventConstant.REFUNDED, order, BaseContext.getCurrentId(), reason, null, "employee");
        log.info("退款完成，订单已取消：orderId={}", orderId);
    }

    /**
     * 解析订单的预约时段，解析不出来直接拒绝操作。
     * 绝不能让 start/end 以 null 进入候选 SQL —— 那样 s.start_time &lt; NULL 恒为 false，
     * 「所有家政员」都会变成候选。
     */
    private LocalTime[] requireScheduleRange(Order order) {
        LocalTime[] range = ScheduleTimeUtil.parseRange(order.getScheduleTime());
        if (order.getScheduleDate() == null || range == null) {
            throw new ServiceOrderBusinessException(MessageConstant.ORDER_SCHEDULE_TIME_INVALID);
        }
        return range;
    }

    /**
     * 状态校验失败时给用户的统一提示。
     * 重新查一次订单拿当前状态 —— CAS 失败恰恰说明它已经不是我们刚读到的那个状态了，
     * 拿旧值拼提示会误导用户。
     */
    private String stateChangedMessage(Long orderId) {
        Order latest = orderMapper.getById(orderId);
        return OrderStatusUtil.alreadyMessage(latest == null ? null : latest.getStatus());
    }

    /**
     * 下单用券：校验 → 算折扣 → 把券快照和折后金额写进订单（此时订单还没落库）。
     * <p>
     * voucherId 为 null 时按「不用券」处理，金额原样写入。
     * <b>家政员预约不允许用券</b> —— 由调用方直接传 null 进来，见 submitSingleOrder。
     *
     * @param originalAmount 用券前的原价
     */
    private void applyVoucher(Long userId, Order order, BigDecimal originalAmount, Long voucherId) {
        if (voucherId == null) {
            order.setAmount(originalAmount);
            return;
        }
        // 1) 这张券得确实属于该用户
        UserVoucher userVoucher = userVoucherMapper.getByUserAndVoucher(userId, voucherId);
        if (userVoucher == null) {
            throw new ServiceOrderBusinessException(MessageConstant.USER_VOUCHER_NOT_FOUND);
        }
        if (!UserVoucher.STATUS_UNUSED.equals(userVoucher.getStatus())) {
            throw new ServiceOrderBusinessException(MessageConstant.USER_VOUCHER_ALREADY_USED);
        }
        // 2) 券本身还得是在上架状态
        Voucher voucher = voucherMapper.getById(voucherId);
        if (voucher == null || !Voucher.STATUS_ON_SALE.equals(voucher.getStatus())) {
            throw new ServiceOrderBusinessException(MessageConstant.VOUCHER_NOT_ON_SALE);
        }
        // 3) 算折扣：实付 = 原价 × 折扣率 / 100（actual_value = 80 表示八折）
        BigDecimal finalAmount = originalAmount
                .multiply(BigDecimal.valueOf(voucher.getActualValue()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        // 4) 券信息作为快照写进订单，订单详情直接展示、不用再回查券表
        order.setAmount(finalAmount);
        order.setVoucherId(voucher.getId());
        order.setVoucherTitle(voucher.getTitle());
        order.setVoucherDiscount(voucher.getActualValue());
        order.setDiscountAmount(originalAmount.subtract(finalAmount));
    }

    /**
     * 订单落库后把券置为「已使用」。
     * <p>
     * 用状态CAS（只有「未使用」才能改成「已使用」）：同一张券被两个订单并发使用时只会成功一次，
     * 失败说明券刚被别的订单用掉 —— 抛异常让整个下单事务回滚，不会出现"券被用两次"或"下了单但券没扣"。
     */
    private void consumeVoucher(Long userId, Order order) {
        if (order.getVoucherId() == null) {
            return;
        }
        int rows = userVoucherMapper.markUsed(userId, order.getVoucherId(), order.getId(),
                UserVoucher.STATUS_UNUSED, UserVoucher.STATUS_USED);
        if (rows == 0) {
            throw new ServiceOrderBusinessException(MessageConstant.USER_VOUCHER_ALREADY_USED);
        }
        log.info("订单用券成功：orderId={}, voucherId={}, 优惠{}元",
                order.getId(), order.getVoucherId(), order.getDiscountAmount());
    }

    /**
     * 校验必填原因并做长度保护（对应列 varchar(255)）
     */
    private String requireReason(String reason, String emptyMessage) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new ServiceOrderBusinessException(emptyMessage);
        }
        String trimmed = reason.trim();
        if (trimmed.length() > 255) {
            throw new ServiceOrderBusinessException(MessageConstant.ORDER_REJECT_REASON_TOO_LONG);
        }
        return trimmed;
    }

    // ==================== 工具方法 ====================

    /**
     * 生成订单号：HS + yyyyMMddHHmmss + 4位随机数
     */
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
        // 填充家政员姓名，供员工端展示「已指派（张三）」
        if (order.getHousekeeperId() != null) {
            Housekeeper housekeeper = housekeeperMapper.getById(order.getHousekeeperId());
            if (housekeeper != null) {
                vo.setHousekeeperName(housekeeper.getName());
            }
        }
        return vo;
    }
}
