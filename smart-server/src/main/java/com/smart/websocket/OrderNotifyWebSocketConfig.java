package com.smart.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 订单提醒 WebSocket 配置
 * <p>
 * 注册路径 /ws/order-notify：Web 员工端浏览器连接该地址即可实时收到来单/催单提醒。
 */
@Configuration
@EnableWebSocket
public class OrderNotifyWebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private OrderNotifyWebSocketServer orderNotifyWebSocketServer;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(orderNotifyWebSocketServer, "/ws/order-notify")
                .setAllowedOrigins("*");
    }
}
