package com.CodeEvalCrew.AutoScore.configuration;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;

public class MyWebSocketHandler extends TextWebSocketHandler {
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("Nhận được tin nhắn từ client: " + message.getPayload());
        // Gửi phản hồi lại client
        session.sendMessage(new TextMessage("Server đã nhận: " + message.getPayload()));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Kết nối WebSocket mới được thiết lập");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Kết nối WebSocket đã đóng");
    }
}