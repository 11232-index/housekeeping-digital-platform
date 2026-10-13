package com.smart.websocket;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 订单提醒 WebSocket 服务端
 * <p>
 * 作用：把“来单提醒/客户催单”实时推送给已连接的 Web 员工端（浏览器）。
 * 消息统一为 JSON：{type, title, content, orderId, orderNumber, amount}，
 * 前端根据 type 弹提示并语音播报。
 */
@Slf4j
@Component
public class OrderNotifyWebSocketServer extends TextWebSocketHandler {

    /** 在线会话集合（Web 员工端浏览器连接） */
    private static final CopyOnWriteArraySet<WebSocketSession> SESSIONS = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        SESSIONS.add(session);
        log.info("订单提醒 WebSocket 连接建立，当前在线 {} 个", SESSIONS.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        SESSIONS.remove(session);
        log.info("订单提醒 WebSocket 连接关闭，当前在线 {} 个", SESSIONS.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("订单提醒 WebSocket 传输异常：{}", exception.getMessage());
        SESSIONS.remove(session);
        if (session.isOpen()) {
            session.close();
        }
    }

    /**
     * 广播一条消息给所有在线的 Web 员工端
     */
    public static void broadcast(String jsonMessage) {
        if (SESSIONS.isEmpty()) {
            log.info("订单提醒广播：当前无在线 Web 员工端，跳过。消息：{}", jsonMessage);
            return;
        }
        TextMessage message = new TextMessage(jsonMessage);
        for (WebSocketSession session : SESSIONS) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            } catch (Exception e) {
                log.error("订单提醒发送失败：{}", e.getMessage());
                SESSIONS.remove(session);
            }
        }
    }

    /**
     * 发送“来单提醒 / 客户催单”通知（组装成 JSON 后广播）
     *
     * @param type        消息类型：newOrder 来单提醒；urge 客户催单
     * @param title       提示标题（前端弹窗标题）
     * @param content     播报/提示内容（前端会朗读）
     * @param orderId     订单id
     * @param orderNumber 订单号
     * @param amount      订单金额
     */
    public static void pushOrderNotify(String type, String title, String content,
                                       Long orderId, String orderNumber, BigDecimal amount) {
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("type", type);
        payload.put("title", title);
        payload.put("content", content);
        payload.put("orderId", orderId);
        payload.put("orderNumber", orderNumber);
        payload.put("amount", amount);
        broadcast(JSON.toJSONString(payload));
    }
}
