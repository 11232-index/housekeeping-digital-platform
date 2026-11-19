package com.smart.websocket;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 订单提醒 WebSocket 服务端。
 * <p>
 * 连接按身份分组：
 * <ul>
 *   <li>用户端（token 解析出 userId）→ 只收自己的通知</li>
 *   <li>员工端（token 解析出 empId）→ 收员工端订单提醒</li>
 *   <li>未带 token 的旧版管理后台 → 按员工端处理，保持兼容</li>
 * </ul>
 * 消息统一为 JSON：{type, title, content, orderId, orderNumber, amount}。
 */
@Slf4j
@Component
public class OrderNotifyWebSocketServer extends TextWebSocketHandler {

    /** 旧版/无 token 的员工端连接 */
    private static final Set<WebSocketSession> STAFF_SESSIONS = new CopyOnWriteArraySet<>();

    /** employeeId → 该员工的连接 */
    private static final Map<Long, Set<WebSocketSession>> EMPLOYEE_SESSIONS = new ConcurrentHashMap<>();

    /** userId → 该用户的连接 */
    private static final Map<Long, Set<WebSocketSession>> USER_SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        Object empId = session.getAttributes().get("empId");
        if (userId != null) {
            USER_SESSIONS.computeIfAbsent(toLong(userId), k -> new CopyOnWriteArraySet<>()).add(session);
            log.info("用户端 WebSocket 连接建立：userId={}", userId);
        } else if (empId != null) {
            EMPLOYEE_SESSIONS.computeIfAbsent(toLong(empId), k -> new CopyOnWriteArraySet<>()).add(session);
            log.info("员工端 WebSocket 连接建立：empId={}", empId);
        } else {
            STAFF_SESSIONS.add(session);
            log.info("员工端 WebSocket 连接建立（无 token，按旧版后台处理），当前在线 {} 个", STAFF_SESSIONS.size());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        removeSession(session);
        log.info("订单提醒 WebSocket 连接关闭：{}", status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("订单提醒 WebSocket 传输异常：{}", exception.getMessage());
        removeSession(session);
        if (session.isOpen()) {
            session.close();
        }
    }

    /**
     * 员工端广播：所有已认证员工 + 旧版后台连接。
     */
    public static void broadcast(String jsonMessage) {
        TextMessage message = new TextMessage(jsonMessage);
        send(STAFF_SESSIONS, message);
        for (Set<WebSocketSession> sessions : EMPLOYEE_SESSIONS.values()) {
            send(sessions, message);
        }
    }

    /**
     * 只推给某个用户（用户端连接）。
     */
    public static void pushToUser(Long userId, String jsonMessage) {
        if (userId == null) {
            return;
        }
        send(USER_SESSIONS.get(userId), new TextMessage(jsonMessage));
    }

    public static void pushOrderNotify(String type, String title, String content,
                                       Long orderId, String orderNumber, BigDecimal amount) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", type);
        payload.put("title", title);
        payload.put("content", content);
        payload.put("orderId", orderId);
        payload.put("orderNumber", orderNumber);
        payload.put("amount", amount);
        broadcast(JSON.toJSONString(payload));
    }

    public static void pushUserNotify(Long userId, String type, String title, String content,
                                      Long orderId, String orderNumber) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", type);
        payload.put("title", title);
        payload.put("content", content);
        payload.put("orderId", orderId);
        payload.put("orderNumber", orderNumber);
        pushToUser(userId, JSON.toJSONString(payload));
    }

    private static void send(Set<WebSocketSession> sessions, TextMessage message) {
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        for (WebSocketSession session : new HashSet<>(sessions)) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            } catch (Exception e) {
                log.error("订单提醒发送失败：{}", e.getMessage());
                removeSession(session);
            }
        }
    }

    private static void removeSession(WebSocketSession session) {
        STAFF_SESSIONS.remove(session);
        removeFromMap(EMPLOYEE_SESSIONS, session);
        removeFromMap(USER_SESSIONS, session);
    }

    private static void removeFromMap(Map<Long, Set<WebSocketSession>> map, WebSocketSession session) {
        for (Map.Entry<Long, Set<WebSocketSession>> entry : map.entrySet()) {
            entry.getValue().remove(session);
            if (entry.getValue().isEmpty()) {
                map.remove(entry.getKey());
            }
        }
    }

    private static Long toLong(Object value) {
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
