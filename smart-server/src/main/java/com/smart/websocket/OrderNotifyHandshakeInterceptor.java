package com.smart.websocket;

import com.smart.properties.JwtProperties;
import com.smart.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * WebSocket 握手解析身份。
 * <p>
 * 连接地址可带 token：
 * <pre>
 * /ws/order-notify?token=xxx   → 员工端
 * /ws/order-notify?token=xxx   → 用户端（claims 里是 userId）
 * </pre>
 * 不带 token 时按“未认证员工端”处理，保持旧版管理后台连接兼容。
 */
@Slf4j
@Component
public class OrderNotifyHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = UriComponentsBuilder.fromUri(request.getURI())
                .build().getQueryParams().getFirst("token");
        if (token == null || token.trim().isEmpty()) {
            return true;
        }
        token = token.trim();

        // 员工端令牌：claims 里是 empId
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getEmployeeSecretKey(), token);
            Object empId = claims.get("empId");
            if (empId != null) {
                attributes.put("empId", Long.valueOf(String.valueOf(empId)));
                return true;
            }
        } catch (Exception ignored) {
            // 继续按用户令牌解析
        }

        // 用户端令牌：claims 里是 userId
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Object userId = claims.get("userId");
            if (userId != null) {
                attributes.put("userId", Long.valueOf(String.valueOf(userId)));
                return true;
            }
        } catch (Exception e) {
            log.warn("WebSocket token 解析失败，按匿名员工端连接处理：{}", e.getMessage());
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 无需处理
    }
}
