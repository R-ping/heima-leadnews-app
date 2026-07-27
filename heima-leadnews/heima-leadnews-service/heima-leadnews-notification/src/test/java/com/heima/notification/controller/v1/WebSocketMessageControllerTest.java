package com.heima.notification.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.notification.dtos.ImMessageDto;
import com.heima.notification.service.ImService;
import com.heima.notification.websocket.SessionManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketMessageController 单元测试")
class WebSocketMessageControllerTest {

    @Mock
    private ImService imService;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketMessageController controller;

    private static final Long SENDER_ID = 1001L;
    private static final Long RECEIVER_ID = 2002L;

    private Principal mockPrincipal() {
        return () -> SENDER_ID.toString();
    }

    // ==================== handleMessage ====================

    @Nested
    @DisplayName("handleMessage 方法测试")
    class HandleMessageTests {

        @Test
        @DisplayName("成功流程 - 接收者在线，发送ACK并推送消息给接收者")
        void shouldSendAckAndPushWhenSuccessAndReceiverOnline() {
            // given
            Map<String, Object> payload = buildPayload(SENDER_ID, RECEIVER_ID, "Hello", 1);
            Map<String, Object> data = new HashMap<>();
            data.put("message_id", 5001L);
            data.put("created_at", "2025-01-01T12:00:00");
            ResponseResult<Map<String, Object>> result = ResponseResult.okResult(data);
            when(imService.sendMessage(eq(SENDER_ID), any(ImMessageDto.class))).thenReturn(result);
            when(sessionManager.isOnline(RECEIVER_ID)).thenReturn(true);

            // when
            controller.handleMessage(payload, mockPrincipal());

            // then
            verify(imService).sendMessage(eq(SENDER_ID), any(ImMessageDto.class));

            // 验证ACK发送给发送者
            ArgumentCaptor<Map<String, Object>> ackCaptor = ArgumentCaptor.forClass(Map.class);
            verify(messagingTemplate).convertAndSendToUser(
                    eq(SENDER_ID.toString()), eq("/queue/messages"), ackCaptor.capture());
            Map<String, Object> ack = ackCaptor.getValue();
            assertEquals("MESSAGE_ACK", ack.get("type"));
            assertEquals(5001L, ack.get("message_id"));
            assertEquals("sent", ack.get("status"));

            // 验证推送发送给接收者
            ArgumentCaptor<Map<String, Object>> pushCaptor = ArgumentCaptor.forClass(Map.class);
            verify(messagingTemplate).convertAndSendToUser(
                    eq(RECEIVER_ID.toString()), eq("/queue/messages"), pushCaptor.capture());
            Map<String, Object> push = pushCaptor.getValue();
            assertEquals("MESSAGE_RECEIVED", push.get("type"));
            assertEquals(5001L, push.get("message_id"));
            assertEquals(SENDER_ID, push.get("sender_id"));
            assertEquals("Hello", push.get("content"));
        }

        @Test
        @DisplayName("成功流程 - 接收者离线，只发送ACK不推送")
        void shouldSendAckOnlyWhenSuccessAndReceiverOffline() {
            // given
            Map<String, Object> payload = buildPayload(SENDER_ID, RECEIVER_ID, "Hello", 1);
            Map<String, Object> data = new HashMap<>();
            data.put("message_id", 5002L);
            data.put("created_at", "2025-01-01T12:00:00");
            ResponseResult<Map<String, Object>> result = ResponseResult.okResult(data);
            when(imService.sendMessage(eq(SENDER_ID), any(ImMessageDto.class))).thenReturn(result);
            when(sessionManager.isOnline(RECEIVER_ID)).thenReturn(false);

            // when
            controller.handleMessage(payload, mockPrincipal());

            // then
            // 验证ACK发送给发送者
            verify(messagingTemplate).convertAndSendToUser(
                    eq(SENDER_ID.toString()), eq("/queue/messages"), any(Map.class));

            // 验证没有推送发送给接收者
            verify(messagingTemplate, never()).convertAndSendToUser(
                    eq(RECEIVER_ID.toString()), eq("/queue/messages"), any(Map.class));
        }

        @Test
        @DisplayName("错误流程 - 服务返回非200，发送错误消息给发送者")
        void shouldSendErrorWhenResponseCodeNot200() {
            // given
            Map<String, Object> payload = buildPayload(SENDER_ID, RECEIVER_ID, "Hello", 1);
            ResponseResult<Map<String, Object>> result = ResponseResult.errorResult(403, "禁止发送");
            when(imService.sendMessage(eq(SENDER_ID), any(ImMessageDto.class))).thenReturn(result);

            // when
            controller.handleMessage(payload, mockPrincipal());

            // then
            ArgumentCaptor<Map<String, Object>> errorCaptor = ArgumentCaptor.forClass(Map.class);
            verify(messagingTemplate).convertAndSendToUser(
                    eq(SENDER_ID.toString()), eq("/queue/messages"), errorCaptor.capture());
            Map<String, Object> error = errorCaptor.getValue();
            assertEquals("MESSAGE_ERROR", error.get("type"));
            assertEquals(403, error.get("code"));
            assertEquals("禁止发送", error.get("message"));
        }

        @Test
        @DisplayName("msg_type 为 null - 默认设为 1")
        void shouldDefaultMsgTypeToOneWhenMsgTypeIsNull() {
            // given
            Map<String, Object> payload = new HashMap<>();
            payload.put("sender_id", SENDER_ID);
            payload.put("receiver_id", RECEIVER_ID);
            payload.put("content", "Hello");
            // msg_type not set (null)
            Map<String, Object> data = new HashMap<>();
            data.put("message_id", 5003L);
            data.put("created_at", "2025-01-01T12:00:00");
            ResponseResult<Map<String, Object>> result = ResponseResult.okResult(data);
            when(imService.sendMessage(eq(SENDER_ID), any(ImMessageDto.class))).thenReturn(result);
            when(sessionManager.isOnline(RECEIVER_ID)).thenReturn(true);

            // when
            controller.handleMessage(payload, mockPrincipal());

            // then
            ArgumentCaptor<ImMessageDto> dtoCaptor = ArgumentCaptor.forClass(ImMessageDto.class);
            verify(imService).sendMessage(eq(SENDER_ID), dtoCaptor.capture());
            assertEquals(1, dtoCaptor.getValue().getMsgType());
        }

        @Test
        @DisplayName("content 为 null - 传入 null 给 service")
        void shouldPassNullContentWhenContentIsNull() {
            // given
            Map<String, Object> payload = new HashMap<>();
            payload.put("sender_id", SENDER_ID);
            payload.put("receiver_id", RECEIVER_ID);
            payload.put("content", null);
            payload.put("msg_type", 1);
            Map<String, Object> data = new HashMap<>();
            data.put("message_id", 5004L);
            data.put("created_at", "2025-01-01T12:00:00");
            ResponseResult<Map<String, Object>> result = ResponseResult.okResult(data);
            when(imService.sendMessage(eq(SENDER_ID), any(ImMessageDto.class))).thenReturn(result);
            when(sessionManager.isOnline(RECEIVER_ID)).thenReturn(true);

            // when
            controller.handleMessage(payload, mockPrincipal());

            // then
            ArgumentCaptor<ImMessageDto> dtoCaptor = ArgumentCaptor.forClass(ImMessageDto.class);
            verify(imService).sendMessage(eq(SENDER_ID), dtoCaptor.capture());
            assertNull(dtoCaptor.getValue().getContent());
        }

        @Test
        @DisplayName("sender_id 为 null - 抛出 NullPointerException")
        void shouldThrowNpeWhenSenderIdIsNull() {
            Map<String, Object> payload = new HashMap<>();
            payload.put("sender_id", null);
            payload.put("receiver_id", RECEIVER_ID);
            payload.put("content", "Hello");

            assertThrows(NullPointerException.class,
                    () -> controller.handleMessage(payload, mockPrincipal()));
            verifyNoInteractions(imService);
            verifyNoInteractions(messagingTemplate);
        }

        @Test
        @DisplayName("receiver_id 为 null - 抛出 NullPointerException")
        void shouldThrowNpeWhenReceiverIdIsNull() {
            Map<String, Object> payload = new HashMap<>();
            payload.put("sender_id", SENDER_ID);
            payload.put("receiver_id", null);
            payload.put("content", "Hello");

            assertThrows(NullPointerException.class,
                    () -> controller.handleMessage(payload, mockPrincipal()));
            verifyNoInteractions(imService);
            verifyNoInteractions(messagingTemplate);
        }
    }

    // ==================== handleReadReceipt ====================

    @Nested
    @DisplayName("handleReadReceipt 方法测试")
    class HandleReadReceiptTests {

        @Test
        @DisplayName("发送者在线 - 推送已读回执")
        void shouldSendReadReceiptWhenSenderOnline() {
            // given
            Map<String, Object> payload = new HashMap<>();
            payload.put("reader_id", RECEIVER_ID);
            payload.put("session_id", 100L);
            payload.put("last_read_id", 500L);
            payload.put("sender_id", SENDER_ID);
            when(sessionManager.isOnline(SENDER_ID)).thenReturn(true);

            // when
            controller.handleReadReceipt(payload, mockPrincipal());

            // then
            ArgumentCaptor<Map<String, Object>> receiptCaptor = ArgumentCaptor.forClass(Map.class);
            verify(messagingTemplate).convertAndSendToUser(
                    eq(SENDER_ID.toString()), eq("/queue/messages"), receiptCaptor.capture());
            Map<String, Object> receipt = receiptCaptor.getValue();
            assertEquals("READ_RECEIPT", receipt.get("type"));
            assertEquals(100L, receipt.get("session_id"));
            assertEquals(RECEIVER_ID, receipt.get("reader_id"));
            assertEquals(500L, receipt.get("last_read_id"));
        }

        @Test
        @DisplayName("发送者离线 - 不推送已读回执")
        void shouldNotSendReadReceiptWhenSenderOffline() {
            // given
            Map<String, Object> payload = new HashMap<>();
            payload.put("reader_id", RECEIVER_ID);
            payload.put("session_id", 100L);
            payload.put("last_read_id", 500L);
            payload.put("sender_id", SENDER_ID);
            when(sessionManager.isOnline(SENDER_ID)).thenReturn(false);

            // when
            controller.handleReadReceipt(payload, mockPrincipal());

            // then
            verify(messagingTemplate, never()).convertAndSendToUser(
                    anyString(), anyString(), any(Map.class));
        }
    }

    // ==================== helper ====================

    private Map<String, Object> buildPayload(Long senderId, Long receiverId, String content, Integer msgType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sender_id", senderId);
        payload.put("receiver_id", receiverId);
        payload.put("content", content);
        payload.put("msg_type", msgType);
        return payload;
    }
}