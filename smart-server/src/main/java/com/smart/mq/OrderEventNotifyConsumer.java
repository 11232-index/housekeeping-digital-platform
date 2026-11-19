package com.smart.mq;

import com.rabbitmq.client.Channel;
import com.smart.config.RabbitMQConfiguration;
import com.smart.constant.OrderEventConstant;
import com.smart.entity.Housekeeper;
import com.smart.entity.Order;
import com.smart.event.OrderEvent;
import com.smart.mapper.HousekeeperMapper;
import com.smart.service.OrderNotifyService;
import com.smart.service.UserNotifyService;
import com.smart.websocket.OrderNotifyWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * 订单事件通知消费者。
 * <p>
 * 订单主流程只发事件，通知落库和 WebSocket 推送统一在这里做：
 * <ul>
 *   <li>员工端通知 → order_notify + 在线员工端广播</li>
 *   <li>用户端通知 → user_notify + 按 userId 定向推送（有连接时）</li>
 * </ul>
 * 失败不重新入队，进入 order.dlq，避免阻塞后续订单事件。
 */
@Slf4j
@Component
public class OrderEventNotifyConsumer {

    private static final DateTimeFormatter ARRIVAL_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private OrderNotifyService orderNotifyService;

    @Autowired
    private UserNotifyService userNotifyService;

    @Autowired
    private HousekeeperMapper housekeeperMapper;

    @RabbitListener(queues = RabbitMQConfiguration.QUEUE_ORDER_NOTIFY)
    public void onOrderEvent(OrderEvent event,
                             Channel channel,
                             @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            handle(event);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理订单事件失败：eventId={}, type={}, error={}",
                    event.getEventId(), event.getEventType(), e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void handle(OrderEvent event) {
        String type = event.getEventType();
        Long orderId = event.getOrderId();
        String number = event.getOrderNumber();
        log.info("处理订单事件：eventId={}, type={}, orderId={}, status={}, operator={}",
                event.getEventId(), type, orderId, event.getStatus(), event.getOperator());

        switch (type) {
            case OrderEventConstant.CREATED:
                orderNotifyService.push("newOrder", "来单提醒",
                        "您有新的订单，订单号 " + number,
                        orderId, number, event.getAmount());
                break;

            case OrderEventConstant.ASSIGNED:
                String hkName = "家政员";
                if (event.getHousekeeperId() != null) {
                    Housekeeper hk = housekeeperMapper.getById(event.getHousekeeperId());
                    if (hk != null && hk.getName() != null) {
                        hkName = hk.getName();
                    }
                }
                orderNotifyService.push("assign", "订单已指派",
                        "订单 " + number + " 已指派给 " + hkName + "，等待其确认接单",
                        orderId, number, event.getAmount());
                break;

            case OrderEventConstant.ACCEPTED:
                String arrival = event.getEstimatedServiceTime() == null
                        ? "" : event.getEstimatedServiceTime().format(ARRIVAL_FORMATTER);
                orderNotifyService.push("accept", "家政员已接单",
                        "订单 " + number + " 家政员已接单，预计到达 " + arrival,
                        orderId, number, event.getAmount());
                pushUser(event, "accepted", "家政员已接单",
                        "您的订单 " + number + " 已被接单，预计到达 " + arrival);
                break;

            case OrderEventConstant.ARRIVED:
                pushUser(event, "inService", "家政员已到达",
                        "您的订单 " + number + " 家政员已到达，开始服务");
                break;

            case OrderEventConstant.COMPLETED:
                pushUser(event, "completed", "服务已完成",
                        "您的订单 " + number + " 已完成，感谢您的使用");
                break;

            case OrderEventConstant.CANCELLED:
                handleCancelled(event);
                break;

            case OrderEventConstant.REFUNDED:
                pushUser(event, "refunded", "退款已完成",
                        "您的订单 " + number + " 的退款已处理完成");
                break;

            case OrderEventConstant.PAID:
            default:
                log.debug("事件无需生成通知：type={}, orderId={}", type, orderId);
                break;
        }
    }

    private void pushUser(OrderEvent event, String type, String title, String content) {
        userNotifyService.push(event.getUserId(), type, title, content,
                event.getOrderId(), event.getOrderNumber());
        OrderNotifyWebSocketServer.pushUserNotify(event.getUserId(), type, title, content,
                event.getOrderId(), event.getOrderNumber());
    }

    private void handleCancelled(OrderEvent event) {
        String number = event.getOrderNumber();
        String reason = event.getReason() == null ? "" : event.getReason();
        String operator = event.getOperator();

        if ("employee".equals(operator)) {
            // 员工取消：通知用户进入退款
            pushUser(event, "cancelled", "订单已取消",
                    "您的订单 " + number + " 已取消（" + reason + "），退款将尽快处理");
            return;
        }

        if ("worker".equals(operator)) {
            // 家政员无法服务：员工端待退款 + 用户安抚
            orderNotifyService.push("refund", "家政员无法服务",
                    "订单 " + number + " 家政员报告无法服务：" + reason + "，请及时处理退款",
                    event.getOrderId(), number, event.getAmount());
            pushUser(event, "cannotServe", "服务无法继续",
                    "您的订单 " + number + " 因「" + reason + "」无法继续，将为您处理退款");
            return;
        }

        // 用户操作：只有“已支付→申请退款”才需要通知员工；未支付超时/主动取消只改状态
        if (Order.STATUS_REFUNDING.equals(event.getStatus())) {
            orderNotifyService.push("refund", "客户申请退款",
                    "订单 " + number + " 客户申请退款：" + reason + "，请及时处理",
                    event.getOrderId(), number, event.getAmount());
        }
    }
}
