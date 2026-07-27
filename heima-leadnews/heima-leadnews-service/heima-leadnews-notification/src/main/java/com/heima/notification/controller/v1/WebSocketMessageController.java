package com.heima.notification.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.notification.dtos.ImMessageDto;
import com.heima.notification.service.ImService;
import com.heima.notification.websocket.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
public class WebSocketMessageController {

    @Autowired
    private ImService imService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 接收客户端通过WebSocket发送的消息
     * 客户端发送到: /app/im/send
     */
    @MessageMapping("/im/send")
    public void handleMessage(@Payload Map<String, Object> payload, Principal principal) {
        Long senderId = Long.valueOf(payload.get("sender_id").toString());
        Long receiverId = Long.valueOf(payload.get("receiver_id").toString());
        String content = (String) payload.get("content");

        ImMessageDto dto = new ImMessageDto();
        dto.setReceiverId(receiverId);
        dto.setContent(content);
        dto.setMsgType(payload.get("msg_type") != null ? Integer.valueOf(payload.get("msg_type").toString()) : 1);

        // 通过HTTP服务发送消息（存储+状态机校验）
        ResponseResult result = imService.sendMessage(senderId, dto);

        if (result.getCode() == 200 && result.getData() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();

            // 推送给发送者ACK
            Map<String, Object> ack = new HashMap<>();
            ack.put("type", "MESSAGE_ACK");
            ack.put("message_id", data.get("message_id"));
            ack.put("status", "sent");
            messagingTemplate.convertAndSendToUser(senderId.toString(), "/queue/messages", ack);

            // 如果接收者在线，实时推送
            if (sessionManager.isOnline(receiverId)) {
                Map<String, Object> push = new HashMap<>();
                push.put("type", "MESSAGE_RECEIVED");
                push.put("message_id", data.get("message_id"));
                push.put("sender_id", senderId);
                push.put("receiver_id", receiverId);
                push.put("content", content);
                push.put("created_at", data.get("created_at"));
                messagingTemplate.convertAndSendToUser(receiverId.toString(), "/queue/messages", push);
            }
        } else {
            // 发送错误消息
            Map<String, Object> error = new HashMap<>();
            error.put("type", "MESSAGE_ERROR");
            error.put("code", result.getCode());
            error.put("message", result.getMessage());
            messagingTemplate.convertAndSendToUser(senderId.toString(), "/queue/messages", error);
        }
    }

    /**
     * 已读回执
     * 客户端发送到: /app/im/read
     */
    @MessageMapping("/im/read")
    public void handleReadReceipt(@Payload Map<String, Object> payload, Principal principal) {
        Long readerId = Long.valueOf(payload.get("reader_id").toString());
        Long sessionId = Long.valueOf(payload.get("session_id").toString());
        Long lastReadId = Long.valueOf(payload.get("last_read_id").toString());
        Long senderId = Long.valueOf(payload.get("sender_id").toString());

        // 推送已读回执给消息发送者
        Map<String, Object> readReceipt = new HashMap<>();
        readReceipt.put("type", "READ_RECEIPT");
        readReceipt.put("session_id", sessionId);
        readReceipt.put("reader_id", readerId);
        readReceipt.put("last_read_id", lastReadId);

        if (sessionManager.isOnline(senderId)) {
            messagingTemplate.convertAndSendToUser(senderId.toString(), "/queue/messages", readReceipt);
        }
    }
}